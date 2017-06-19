import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryUtil.NULL
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.IOException
import java.io.FileInputStream
import org.lwjgl.system.MemoryStack
import org.lwjgl.opengl.GL11.GL_TRUE
import org.lwjgl.opengl.GL20.GL_COMPILE_STATUS
import org.lwjgl.opengl.GL20.glGetShaderi
import org.lwjgl.opengl.GL20.glLinkProgram
import org.lwjgl.opengl.GL20.glAttachShader
import org.lwjgl.opengl.GL20.glCreateProgram
import org.lwjgl.opengl.GL20.GL_LINK_STATUS
import org.lwjgl.opengl.GL20.glGetProgrami
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import java.awt.SystemColor.window
import org.lwjgl.BufferUtils
import java.nio.IntBuffer



class Engine {

    companion object {

        val WINDOW_SIZE = Pair(1024, 768)

    }

    private var errorCallback : GLFWErrorCallback? = null
    private var keyCallback : GLFWKeyCallback? = null
    var vertexPosition : Int = 0
    var buffer : Int = 0
    var startTime = System.currentTimeMillis()
    var shaderProgram : Int = 0

    private var window : Long? = null

    private fun init() {

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        errorCallback = glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( glfwInit() == false) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        // Configure our window
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        // Create the window
        window = glfwCreateWindow(WINDOW_SIZE.first, WINDOW_SIZE.second, "Hello World!", NULL, NULL)
        if (window == NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        keyCallback = glfwSetKeyCallback(window!!, object : GLFWKeyCallback() {
            override fun invoke(window: kotlin.Long,
                                key: kotlin.Int,
                                scancode: kotlin.Int,
                                action: kotlin.Int,
                                mods: kotlin.Int) {

                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                    glfwSetWindowShouldClose(window, true)
                }

            }
        })

        // Get the resolution of the primary monitor
        val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())

        // Center our window
        glfwSetWindowPos(
                window!!,
                (vidmode.width() - WINDOW_SIZE.first) / 2,
                (vidmode.height() - WINDOW_SIZE.second) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window!!)
        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window!!)

    }

    private fun loadShaders()
    {
        val builder = StringBuilder()

        try {
            FileInputStream("src/main/kotlin/glslsandbox_player/shaders/vertex.glsl").use({ `in` ->
                BufferedReader(InputStreamReader(`in`)).use { reader ->
                    reader.lineSequence().forEach {
                        builder.append(it).append("\n")
                    }
                }
            })
        } catch (ex: IOException) {
            throw RuntimeException("Failed to load a shader file!"
                    + System.lineSeparator() + ex.message)
        }

        var source = builder.toString()
        builder.setLength(0)


        var vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, source)
        glCompileShader(vertexShader)
        var status = glGetShaderi(vertexShader, GL_COMPILE_STATUS)
        if (status != GL_TRUE) {
            throw RuntimeException(glGetShaderInfoLog(vertexShader))
        }

        try {
            FileInputStream("src/main/kotlin/glslsandbox_player/shaders/pixel.glsl").use({ `in` ->
                BufferedReader(InputStreamReader(`in`)).use { reader ->
                    reader.lineSequence().forEach {
                        builder.append(it).append("\n")
                    }
                }
            })
        } catch (ex: IOException) {
            throw RuntimeException("Failed to load a shader file!"
                    + System.lineSeparator() + ex.message)
        }

        source = builder.toString()


        var pixelShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(pixelShader, source)
        glCompileShader(pixelShader)
        status = glGetShaderi(pixelShader, GL_COMPILE_STATUS)
        if (status != GL_TRUE) {
            throw RuntimeException(glGetShaderInfoLog(pixelShader))
        }

        //val vao = glGenVertexArrays()
        //glBindVertexArray(vao)

        val stack = MemoryStack.stackPush()
        val vertices = stack.mallocFloat(3 * 6)
        vertices.put(-1.0f).put(-1.0f).put(1.0f).put(-1.0f).put(-1.0f).put(1.0f)
        vertices.put(1.0f).put(-1.0f).put(1.0f).put(1.0f).put(-1.0f).put(1.0f)
        vertices.flip()

        buffer = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, buffer)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        MemoryStack.stackPop()

        /*
        buffer = gl.createBuffer();
        gl.bindBuffer( gl.ARRAY_BUFFER, buffer );
        gl.bufferData( gl.ARRAY_BUFFER, new Float32Array( [ - 1.0, - 1.0, 1.0, - 1.0, - 1.0, 1.0, 1.0, - 1.0, 1.0, 1.0, - 1.0, 1.0 ] ), gl.STATIC_DRAW );
        */

        shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, pixelShader)
        glLinkProgram(shaderProgram)

        status = glGetProgrami(shaderProgram, GL_LINK_STATUS)
        if (status != GL_TRUE) {
            throw RuntimeException(glGetProgramInfoLog(shaderProgram))
        }

        glUseProgram(shaderProgram);

        vertexPosition = glGetAttribLocation(shaderProgram, "position");
        glEnableVertexAttribArray( vertexPosition )
        glVertexAttribPointer( vertexPosition, 2, GL_FLOAT, false, 0, 0 );
    }

    private fun loop() {

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()

        // Load the shaders man!
        loadShaders()

        // Set the clear color
        glClearColor(1.0f, 1.0f, 0.0f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (glfwWindowShouldClose(window!!) == false ) {

            glBindBuffer( GL_ARRAY_BUFFER, buffer );
            glVertexAttribPointer( vertexPosition, 2, GL_FLOAT, false, 0, 0 );

            // bind pixel shader variables
            var time = System.currentTimeMillis() - startTime
            glUniform1f( glGetUniformLocation( shaderProgram, "time" ), time.toFloat() / 1000 )
            val w = BufferUtils.createIntBuffer(1)
            val h = BufferUtils.createIntBuffer(1)
            glfwGetWindowSize(window!!, w, h)
            val width = w.get(0).toFloat()
            val height = h.get(0).toFloat()
            glUniform2f( glGetUniformLocation( shaderProgram, "resolution" ), width, height );
            // Clear the framebuffer
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            glDrawArrays( GL_TRIANGLES, 0, 6 );

            // Swap the color buffers
            glfwSwapBuffers(window!!);

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }

    }

    fun run() {

        try {

            init()
            loop()
            // Destroy window
            glfwDestroyWindow(window!!);
            keyCallback?.free()

        } finally {

            // Terminate GLFW
            glfwTerminate()
            errorCallback?.free()

        }
    }

}


fun main(args: Array<String>) {

    val engine = Engine()
    engine.run()

}