package com.lbi.ss.logospheres;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import codeanticode.glgraphics.*;

import remixlab.proscene.*;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

public class LogoSpheres extends PApplet
{
	// --------------------------------------------------------------------------------------------------------
	// MAIN AND VERS

	public static void main(String args[]) 
	{
		PApplet.main(new String[] { "--present", "com.lbi.ss.logospheres.LogoSpheres" } );
	}

	List<PVector> points   =	new ArrayList<PVector>();
	List<GLModel> balls    =  	new ArrayList<GLModel>();

	int				CAM_DISTANCE		=	3500;
	int				LIGHT_DISTANCE		=	750;
	int				PUSH_FORCE			=	7500;
	int          	SPHERE_RADIUS       =  	15;
	int          	SPEHERE_DETAIL      =  	10;
	int         	BUFFER              =  	100;
	int           	DEPTH               =  	5;
	int           	SPIN_RADIUS         =  	5;
	float         	DELTA               =  	0.02f;
	float			SCREEN_OFFSET		=	1;
	int           	NUM_SPHERES;
	float			SIZE_OFFSET;

	GLGraphics		        renderer;
	GLGraphicsOffScreen     canvas;
	Scene                   scene;
	GLModel					light1;
	GLModel					light2;

	PImage      logo;
	PVector		pt;	

	int		half_width;
	int		half_height;
	int     lightX, lightY, lightZ;
	int     camX, camY, camZ;

	// --------------------------------------------------------------------------------------------------------
	// PROCESSING

	public void setup()
	{
		size( 
				PApplet.floor(screen.width * SCREEN_OFFSET), 
				PApplet.floor(screen.height * SCREEN_OFFSET), 
				GLConstants.GLGRAPHICS 
		);  

		half_width		=	PApplet.floor(width*0.5f);
		half_height		=	PApplet.floor(height*0.5f);

		canvas     		=	new GLGraphicsOffScreen(this, width, height);

		light1			=	createSphere(20, 20);
		light1.setTint(255,200,200);
		light2			=	createSphere(20, 20);
		light2.setTint(255,200,200);

		pt				=	new PVector();

		initScene();
		initImage();
		initBalls();
		initShaders();
	}

	private void initScene()
	{
		scene      =  new Scene(this);
		scene.camera().setPosition(new PVector(0,0,2500));
		scene.camera().setZClippingCoefficient(200);
		scene.setAxisIsDrawn(false);
		scene.setGridIsDrawn(false);

		println(scene.camera().upVector() );

		//scene.setFrameRate(100);
		//scene.disableMouseHandling();
	}

	public void draw()
	{
		//set vars
		camX = PApplet.floor( PApplet.cos(frameCount * .01f) * half_width );//mouseX - half_width;
		camY = PApplet.floor( PApplet.cos(frameCount * .008f) * height ); //mouseY - half_height;
		camZ = PApplet.floor( PApplet.sin(frameCount * .005f) * CAM_DISTANCE );

		lightX = PApplet.floor( PApplet.cos(frameCount * .03f) * (half_width * 0.75f) );//mouseX - half_width;
		lightY = PApplet.floor( PApplet.cos(frameCount * .015f) * (half_height * 0.75f) ); //mouseY - half_height;
		lightZ = PApplet.floor( PApplet.sin(frameCount * .025f) * LIGHT_DISTANCE );

		scene.camera().setPosition( new PVector(camX, camY, camZ) );
		scene.camera().lookAt( new PVector() );		
		scene.camera().setUpVector( 
				new PVector( 
						scene.camera().upVector().x, 
						1, 
						scene.camera().upVector().z ) 
		);

		//draw 				
		renderer = (GLGraphics) this.g;
		renderer.beginGL();

		background(0);

		//set lights
		pushMatrix();		
		translate(lightX, lightY, lightZ);
		light1.render();			
		popMatrix();

		pushMatrix();		
		translate(-lightX, -lightY, -lightZ);
		light2.render();			
		popMatrix();

		lightFalloff(1.0f, 0, 0.0000005f);
		pointLight(255, 255, 255, lightX, lightY, lightZ);
		pointLight(255, 255, 255, -lightX, -lightY, -lightZ);

		for( int i = 0; i < NUM_SPHERES; ++i )
		{
			pt = points.get(i);

			pushMatrix();			

			/*
			float x	= pt.x + (cos( (frameCount+pt.z) * DELTA) * SPIN_RADIUS);
			float y = pt.y + (sin( (frameCount+pt.z) * DELTA) * SPIN_RADIUS);

		    float xDif 		= 	lightX - x;
		    float yDif 		= 	lightY - y;
		    float halfD		=	LIGHT_DISTANCE * .75f;
		    float lz		=	abs(lightZ);
		    float zMap 		= 	PApplet.map( lz > halfD ? halfD : lz, 0, halfD, 1, 0 );
		    float distance 	= 	PApplet.sqrt(xDif*xDif+yDif*yDif);
		    float tempX 	= 	x - ( (PUSH_FORCE * zMap ) / distance)*(xDif/distance);
		    float tempY 	= 	y - ( (PUSH_FORCE * zMap ) / distance)*(yDif/distance);
		    x = (pt.x - x)/2+tempX;
		    y = (pt.y - y)/2+tempY;

			translate( x, y, 0 );
			x	= pt.x + (cos( (frameCount+pt.z) * DELTA) * SPIN_RADIUS);
			y = pt.y + (sin( (frameCount+pt.z) * DELTA) * SPIN_RADIUS);

		    xDif = -lightX - x;
		    yDif = -lightY - y;
		    distance = PApplet.sqrt(xDif*xDif+yDif*yDif);
		    tempX = x - ( (PUSH_FORCE * zMap ) / distance)*(xDif/distance);
		    tempY = y - ( (PUSH_FORCE * zMap ) / distance)*(yDif/distance);
		    x = (pt.x - x)/2+tempX;
		    y = (pt.y - y)/2+tempY;

			translate( x, y, 0 );
			//*/

			translate( 
					pt.x + (cos( (frameCount+pt.z) * DELTA) * SPIN_RADIUS), 
					pt.y + (sin( (frameCount+pt.z) * DELTA) * SPIN_RADIUS), 
					0 
			);

			balls.get(i).render();

			popMatrix();
		}

		noLights();
		renderer.endGL();

		//tracing
		//		println(frameRate);
	}

	private void translateBall(float mx, float my, float mz)
	{




	}

	// ---------------------------------------------------------------
	// SETUP
	// ---------------------------------------------------------------

	void initImage()
	{
		logo = loadImage( "lbi_logo.gif" );

		int numPixels = logo.width * logo.height;
		int row = -1;

		logo.loadPixels();

		for( int i = 0; i < numPixels; ++i )
		{
			int c = logo.pixels[i];

			int x = i % logo.width;
			if (x == 0) row++;
			int y = row;

			if( c < -1 )
			{				
				points.add( 
						new PVector( 
								PApplet.floor( map( x, 0, logo.width, -half_width + BUFFER, half_width-BUFFER ) ), 
								PApplet.floor( map( y, 0, logo.height, -half_height + BUFFER, half_height-BUFFER ) ), 
								random(0,5000) 
						) 
				);
			}

			NUM_SPHERES = points.size();
		}
	}

	void initBalls()
	{
		for( int i = 0; i < NUM_SPHERES; ++i )
		{
			//	    for( int j = 0; j < DEPTH; ++j )
			//	    {
			GLModel ball = createSphere(SPEHERE_DETAIL, SPHERE_RADIUS);
			ball.setTint(255,0,0);
			//ball.setBlendMode(arg0)
			balls.add( ball );
			//	    }
		}
	}

	void initShaders()
	{

	}

	// ---------------------------------------------------------------
	// SPHERE
	// ---------------------------------------------------------------

	float 	SINCOS_PRECISION 	= 0.5f;
	int	 	SINCOS_LENGTH 		= PApplet.floor(360.0f / SINCOS_PRECISION);  

	GLModel createSphere(int detail, float radius)
	{
		float[] cx, cz, sphereX, sphereY, sphereZ;
		float sinLUT[];
		float cosLUT[];
		float delta, angle_step, angle;
		int vertCount, currVert;
		float r;
		int v1, v11, v2, voff;    
		ArrayList vertices;
		ArrayList normals;    

		sinLUT = new float[SINCOS_LENGTH];
		cosLUT = new float[SINCOS_LENGTH];

		for (int i = 0; i < SINCOS_LENGTH; i++) 
		{
			sinLUT[i] = (float) Math.sin(i * PConstants.DEG_TO_RAD * SINCOS_PRECISION);
			cosLUT[i] = (float) Math.cos(i * PConstants.DEG_TO_RAD * SINCOS_PRECISION);
		}  

		delta = SINCOS_LENGTH / detail;
		cx = new float[detail];
		cz = new float[detail];

		// Calc unit circle in XZ plane
		for (int i = 0; i < detail; i++) 
		{
			cx[i] = -cosLUT[(int) (i * delta) % SINCOS_LENGTH];
			cz[i] = sinLUT[(int) (i * delta) % SINCOS_LENGTH];
		}

		// Computing vertexlist vertexlist starts at south pole
		vertCount = detail * (detail - 1) + 2;
		currVert = 0;

		// Re-init arrays to store vertices
		sphereX = new float[vertCount];
		sphereY = new float[vertCount];
		sphereZ = new float[vertCount];
		angle_step = (SINCOS_LENGTH * 0.5f) / detail;
		angle = angle_step;

		// Step along Y axis
		for (int i = 1; i < detail; i++) 
		{
			float curradius = sinLUT[(int) angle % SINCOS_LENGTH];
			float currY = -cosLUT[(int) angle % SINCOS_LENGTH];
			for (int j = 0; j < detail; j++) 
			{
				sphereX[currVert] = cx[j] * curradius;
				sphereY[currVert] = currY;
				sphereZ[currVert++] = cz[j] * curradius;
			}
			angle += angle_step;
		}

		vertices = new ArrayList();
		normals = new ArrayList();

		r = radius;

		// Add the southern cap    
		for (int i = 0; i < detail; i++) 
		{
			addVertex(vertices, normals, 0.0f, -r, 0.0f);
			addVertex(vertices, normals, sphereX[i] * r, sphereY[i] * r, sphereZ[i] * r);        
		}
		addVertex(vertices, normals, 0.0f, -r, 0.0f);
		addVertex(vertices, normals, sphereX[0] * r, sphereY[0] * r, sphereZ[0] * r);

		// Middle rings
		voff = 0;    
		for (int i = 2; i < detail; i++) 
		{
			v1 = v11 = voff;
			voff += detail;
			v2 = voff;
			for (int j = 0; j < detail; j++) 
			{
				addVertex(vertices, normals, sphereX[v1] * r, sphereY[v1] * r, sphereZ[v1++] * r);
				addVertex(vertices, normals, sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2++] * r);
			}

			// Close each ring
			v1 = v11;
			v2 = voff;
			addVertex(vertices, normals, sphereX[v1] * r, sphereY[v1] * r, sphereZ[v1] * r);
			addVertex(vertices, normals, sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2] * r);
		}

		// Add the northern cap
		for (int i = 0; i < detail; i++) 
		{
			v2 = voff + i;
			addVertex(vertices, normals, sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2] * r);
			addVertex(vertices, normals, 0, r, 0);
		}
		addVertex(vertices, normals, sphereX[voff] * r, sphereY[voff] * r, sphereZ[voff] * r);


		GLModel model = new GLModel(this, vertices.size(), TRIANGLE_STRIP, GLModel.STATIC);

		// Sets the coordinates.
		model.updateVertices(vertices);    

		// Sets the normals.    
		model.initNormals();
		model.updateNormals(normals);    

		return model;
	}

	void addVertex(ArrayList vertices, ArrayList normals, float x, float y, float z)
	{
		PVector vert = new PVector(x, y, z);
		PVector vertNorm = PVector.div(vert, vert.mag()); 
		vertices.add(vert);
		normals.add(vertNorm);
	}

}
