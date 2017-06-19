#ifdef GL_ES
precision mediump float;
#endif

#extension GL_OES_standard_derivatives : enable

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

const vec3 white = vec3(0.83, 0.83, 0.83);
const vec3 blue = vec3(0.1,0.2,0.7);
const vec3 yellow = vec3(0.95,0.95,0.1);

float line (vec2 p, vec2 a, vec2 b) {
	p -= a;
	b -= a;
	a = p - b * dot (p, b) / dot (b , b);
	b *= 0.5;
	p = abs (p - b) - abs (b);
	return max (length (a), max (p.x, p.y));
}

float circle (vec2 p, vec2 o, float r) {
	return abs (length (p - o) - r);
}


void cast_light( void ) {
	float fVisionRange = 64.0;
	float xsize = 32.0;
	float ysize = 32.0;
	vec2 fUV = vec2(gl_FragCoord.x, gl_FragCoord.y);
	vec2 fMousePosition = mouse*resolution;

	float smoothBegin = fVisionRange;
	float smoothEnd = fVisionRange*2.0 + 0.0;
	float multiplier = 1.0;
	float darknessValue = 0.2;
	float dist = length(vec2(fUV.x - fMousePosition.x, fUV.y - fMousePosition.y));
	if (dist > smoothBegin && dist < smoothEnd)
	{
		multiplier = (1.0 + darknessValue) -((dist-smoothBegin)/(smoothEnd-smoothBegin));
	}
	else if (dist > smoothEnd)
	{
		multiplier = darknessValue;
	}

	gl_FragColor *= multiplier;



}

#define hash(a) fract(sin(a)*12345.0)
#define snoise(p) ((old_noise(p, 883.0, 971.0) + old_noise(p + 0.5, 113.0, 157.0)) * 0.5)
float old_noise(vec3 x, float c1, float c2) {
    vec3 p = floor(x);
    vec3 f = fract(x);
    f = f*f*(3.0-2.0*f);
    float n = p.x + p.y*c2+ c1*p.z;
    return mix(
        mix(
            mix(hash(n+0.0),hash(n+1.0),f.x),
            mix(hash(n+c2),hash(n+c2+1.0),f.x),
            f.y),
        mix(
            mix(hash(n+c1),hash(n+c1+1.0),f.x),
            mix(hash(n+c1+c2),hash(n+c1+c2+1.0),f.x),
            f.y),
        f.z);
}


void draw_stars( void )
{
	vec2 position = gl_FragCoord.xy / resolution.xy;


	vec3 color = vec3(0.0);
	float blueness = snoise(400.0 * vec3(position.x * resolution.x / resolution.y, position.y, 0.0));
	color += mix(vec3(1.0), vec3(0.6, 0.9, 1.5), blueness) * smoothstep(0.75, 0.8,  0.1 * snoise(8.0 * vec3(position.x * 10.0 * resolution.x / resolution.y, position.y, time * 0.1)) + 0.9 * snoise(500.0 * vec3(position.x * resolution.x / resolution.y, position.y, 0.0)));

	gl_FragColor = vec4(color, 1.0 );
}

void main( void ) {

	vec2 p = 4.0*( gl_FragCoord.xy / resolution.xy ) -2.0;
	p.x *= resolution.x/resolution.y;

	p.x += sin(p.y+time*2.0)*.05;
	p.y += sin(p.x*2.0-time*2.0)*.2;

	vec3 col = white;

	vec2 frag = p;
	frag = 3.0 * frag + vec2 (2.6, 1.1);

	float d1 = max (circle (frag, vec2 (p.x + 2.6, p.y + 1.3), 0.3), 2.5 - frag.x);
	d1 = min (d1, line (frag, vec2 (p.x + 2.4, 0.6 + p.y), vec2 (p.x + 2.4, 1.6 + p.y)));
	d1 = min (d1, line (frag, vec2 (p.x + 2.8, 1.0 + p.y), vec2 (p.x + 3.0, 0.6 + p.y)));




	d1 -= 0.16;
	float tint = smoothstep (0., 0.0, d1);


	float cc = sqrt(pow(frag.x - 2.6,2.0)+pow(frag.y - 1.0,2.0));
	if (cc > 1.4)
	col = vec3(cc, 0., 0.);
	else
	col = vec3(1, tint, tint);



	if(abs(p.x) > 1.60) col = vec3(0.0);
	if(abs(p.y) > 1.0) col = vec3(0.0);

	gl_FragColor = vec4(col, 1.0);

	if(abs(p.x) > 1.60 || abs(p.y) > 1.0)
		draw_stars();
	//cast_light();
}