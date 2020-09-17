package com.freewinesearcher.online.winebottle;


import javax.imageio.ImageIO;
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Material;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.*;
import javax.media.j3d.*;

import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.image.TextureLoader;

import java.applet.Applet;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.io.*;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;

import com.sun.j3d.utils.image.TextureLoader;


public class Bottle  {
	public static enum glasscolors {WHITE,GREEN};
	public glasscolors glasscolor;
	public static enum winecolors {RED,WHITE,WHITESWEET,ROSE,SHADE}
	public winecolors winecolor;
	public Label label;

	public Bottle(Label label){
		this.label=label;
	}
	
	public TransformGroup getShape(){
		TransformGroup tg=new TransformGroup();
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		Appearance glassAppearance = glassAppearance();
		Cylinder bottle=new Cylinder((label.radius-0.001f), label.radius*8 ,Primitive.GENERATE_NORMALS,80,2,glassAppearance);
		Cylinder neck=new Cylinder((label.radius*0.3f-0.001f), 0.4f ,Primitive.GENERATE_NORMALS,80,2,glassAppearance);
		TransformGroup neckshift = new TransformGroup();
		Transform3D tfneck=new Transform3D();
		tfneck.setTranslation(new Vector3d(0f,1f,0f));
		neckshift.setTransform(tfneck);
		neckshift.addChild(neck);
		if (!winecolor.equals(winecolors.SHADE)) tg.addChild(bottle);
		//tg.addChild(neckshift);
		Appearance wineAppearance = wineAppearance();
		Cylinder wine=new Cylinder((label.radius-0.001f)*0.99f, label.radius*8 ,Primitive.GENERATE_NORMALS,80,2,wineAppearance);
		tg.addChild(wine);
		return tg;
	}

	public Material greenglass(){


		Material glass = new Material();
		glass.setAmbientColor(0.0f,0.0f,0.0f);
		glass.setDiffuseColor(0f,0.1f,0f);
		glass.setEmissiveColor(0f,0f,0f);
		glass.setSpecularColor(0.9f,0.8f,0.3f);
		glass.setLightingEnable(true);
		glass.setShininess(128f);
		return glass;
	}

	public Material lightgreenglass(){


		Material glass = new Material();
		glass.setAmbientColor(0.2f,0.2f,0.0f);
		glass.setDiffuseColor(0f,0.1f,0f);
		glass.setEmissiveColor(0f,0f,0f);
		glass.setSpecularColor(0.6f,0.6f,0.3f);
		glass.setLightingEnable(true);
		glass.setShininess(128f);
		return glass;
	}

	public Material whiteglass(){


		Material glass = new Material();
		glass.setAmbientColor(0.5f,0.5f,0.5f);
		glass.setDiffuseColor(0.1f,0.1f,0.1f);
		glass.setEmissiveColor(0f,0f,0f);
		glass.setSpecularColor(0.8f,0.8f,0.8f);
		glass.setLightingEnable(true);
		glass.setShininess(128f);
		return glass;
	}

	public Material redWine(){


		Material wine = new Material();
		wine.setAmbientColor(0.1f,0.1f,0.1f);
		wine.setDiffuseColor(0f,0f,0f);
		wine.setEmissiveColor(0f,0f,0f);
		wine.setSpecularColor(0.0f,0f,0f);
		wine.setLightingEnable(true);
		wine.setShininess(2f);
		return wine;
	}

	public Material whiteWine(){


		Material wine = new Material();
		wine.setAmbientColor(0.2f,0.2f,0.1f);
		wine.setDiffuseColor(0f,0f,0f);
		wine.setEmissiveColor(0f,0f,0f);
		wine.setSpecularColor(0.00f,0f,0f);
		wine.setLightingEnable(true);
		wine.setShininess(2f);
		return wine;
	}

	public Material sweetwhiteWine(){


		Material wine = new Material();
		wine.setAmbientColor(1f,0.9f,0.1f);
		wine.setDiffuseColor(0f,0f,0f);
		wine.setEmissiveColor(0.4f,0.4f,0f);
		wine.setSpecularColor(0.0f,0f,0f);
		wine.setLightingEnable(true);
		wine.setShininess(2f);
		return wine;
	}

	Appearance glassAppearance(){

		Appearance twistAppear = new Appearance();
		TexCoordGeneration tcg=new TexCoordGeneration();
		Vector4f planeS=new Vector4f(1f,0f,0f,0.5f);
		Vector4f planeT=new Vector4f(0f,1f,0f,0.5f);
		tcg.setPlaneS(planeS);
		tcg.setPlaneT(planeT);
		tcg.setGenMode(TexCoordGeneration.OBJECT_LINEAR+TexCoordGeneration.TEXTURE_COORDINATE_2);
		TextureAttributes ta=new TextureAttributes();
		ta.setTextureMode(TextureAttributes.DECAL);

		TransparencyAttributes transpa = new TransparencyAttributes();
		transpa.setCapability(TransparencyAttributes.BLEND_ONE);
		transpa.setTransparencyMode(TransparencyAttributes.NICEST);

		if (glasscolor==glasscolors.GREEN){
			twistAppear.setMaterial(greenglass());
			transpa.setTransparency(0.2f);
			}
		if (glasscolor==glasscolors.WHITE){
			twistAppear.setMaterial(whiteglass());
			transpa.setTransparency(0.8f);
			}
		if (winecolor==winecolors.WHITE){
			twistAppear.setMaterial(lightgreenglass());
			transpa.setTransparency(0.4f);
		} 
		if (winecolor==winecolors.WHITESWEET){
			twistAppear.setMaterial(whiteglass());
			transpa.setTransparency(0.6f);
		} 
		twistAppear.setTransparencyAttributes(transpa);
		twistAppear.setColoringAttributes(new ColoringAttributes(1f,1f,1f,ColoringAttributes.SHADE_GOURAUD));
	
		return twistAppear;
	}

	Appearance wineAppearance(){

		Appearance twistAppear = new Appearance();
		TexCoordGeneration tcg=new TexCoordGeneration();
		Vector4f planeS=new Vector4f(1f,0f,0f,0.5f);
		Vector4f planeT=new Vector4f(0f,1f,0f,0.5f);
		tcg.setPlaneS(planeS);
		tcg.setPlaneT(planeT);
		tcg.setGenMode(TexCoordGeneration.OBJECT_LINEAR+TexCoordGeneration.TEXTURE_COORDINATE_2);
		TextureAttributes ta=new TextureAttributes();
		ta.setTextureMode(TextureAttributes.DECAL);

		TransparencyAttributes transpa = new TransparencyAttributes();
		transpa.setCapability(TransparencyAttributes.BLEND_ONE);
		transpa.setTransparencyMode(TransparencyAttributes.NICEST);

		if (winecolor==winecolors.RED){
			twistAppear.setMaterial(redWine());
			transpa.setTransparency(0.4f);
		}
		if (winecolor==winecolors.WHITE){
			twistAppear.setMaterial(whiteWine());
			transpa.setTransparency(0.9f);
		}
		if (winecolor==winecolors.WHITESWEET){
			twistAppear.setMaterial(sweetwhiteWine());
			transpa.setTransparency(0.6f);
		}
		if (winecolor==winecolors.SHADE){
			twistAppear.setMaterial(redWine());
			transpa.setTransparency(0.8f);
		}
		twistAppear.setTransparencyAttributes(transpa);
		twistAppear.setColoringAttributes(new ColoringAttributes(1f,1f,1f,ColoringAttributes.SHADE_GOURAUD));
		
		return twistAppear;
	}


}
