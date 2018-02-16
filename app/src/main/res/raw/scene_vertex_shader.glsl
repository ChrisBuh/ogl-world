// Based on http://blog.shayanjaved.com/2011/03/13/shaders-android/
// from Shayan Javed

uniform mat4 u_Matrix;
uniform mat4 u_MVMatrix;
uniform mat4 u_NormalMatrix;

// the shadow projection matrix
uniform mat4 u_ShadowProjMatrix;

// position and normal of the vertices
attribute vec4 a_Position;
attribute vec2 a_TexCoordinate;
attribute vec3 a_Normal;

// to pass on
varying vec3 v_Position;
varying vec2 v_TexCoordinate;
varying vec3 v_Normal;
varying vec4 v_ShadowCoord;


void main() {
	// the vertex position in camera space
	v_Position = vec3(u_MVMatrix * a_Position);

	// the vertex color
	v_TexCoordinate = a_TexCoordinate;

	// the vertex normal coordinate in camera space
	v_Normal = vec3(u_NormalMatrix * vec4(a_Normal, 0.0));

	v_ShadowCoord = u_ShadowProjMatrix * a_Position;

	gl_Position = u_Matrix * a_Position;
}
