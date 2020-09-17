package com.freewinesearcher.online.winebottle;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.GraphicsConfiguration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Webroutines;
import com.freewinesearcher.online.winebottle.Bottle.glasscolors;
import com.freewinesearcher.online.winebottle.Bottle.winecolors;
import com.sun.j3d.utils.applet.MainFrame; 
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.geometry.*;

import javax.imageio.ImageIO;
import javax.media.j3d.*;
import javax.vecmath.*;


public class WineBottle extends Applet implements WindowListener, Runnable {
	public int wineid;
	public int knownwineid;
	public String winename="";
	public String vintage="";
	public String region="";
	public String price="";
	public String url="";
	Bottle.glasscolors glasscolor;
	public Bottle.winecolors winecolor;
	ArrayList<String> ratings=null;
	public static int imagewidth=480;
	public static int imageheight=540;
	static boolean isinitialized=false;
	static SimpleUniverse simpleU ;
	static CapturingCanvas3D canvas3D;
	static MainFrame frame;
	static BranchGroup bg=new BranchGroup();



	public void setColor(String color){
		if (color.equals("RED")) {
			winecolor=winecolors.RED;
			glasscolor=glasscolors.GREEN;
		}
		if (color.equals("WHITE")) {
			winecolor=winecolors.WHITE;
			glasscolor=glasscolors.GREEN;
		}
		if (color.equals("WHITESWEET")) {
			winecolor=winecolors.WHITESWEET;
			glasscolor=glasscolors.WHITE;
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getWinename() {
		return winename;
	}

	public void setWinename(String winename) {
		this.winename = winename;
	}

	public String getVintage() {
		return vintage;
	}

	public void setVintage(String vintage) {
		this.vintage = vintage;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public Bottle.glasscolors getGlasscolor() {
		return glasscolor;
	}

	public void setGlasscolor(Bottle.glasscolors glasscolor) {
		this.glasscolor = glasscolor;
	}

	public Bottle.winecolors getWinecolor() {
		return winecolor;
	}

	public void setWinecolor(Bottle.winecolors winecolor) {
		this.winecolor = winecolor;
	}

	public WineBottle(String winename, String vintage, String region, String price, ArrayList<String> ratings)
	throws HeadlessException {
		super();
		initialize3d();
		this.winename = winename;
		this.vintage = vintage;
		this.region = region;
		this.price=price;
		this.ratings = ratings;
		generate();
	}
	
	public WineBottle() {
		super();
		initialize3d();
	} 
	public WineBottle(int wineid) {
		super();
		initialize3d();
		this.wineid=wineid;
		fillInfo();
	} 


	public void initialize3d(){
		if (!isinitialized){
			bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
			bg.setCapability(BranchGroup.ALLOW_DETACH);
			bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
			
			
			GraphicsConfiguration config =SimpleUniverse.getPreferredConfiguration();
			canvas3D = new CapturingCanvas3D(config);
			setLayout(new BorderLayout());
			add("Center", canvas3D);
			canvas3D.setStereoEnable(false);
			simpleU = new SimpleUniverse(canvas3D);

			simpleU.getViewer().getView().setSceneAntialiasingEnable(true);
			// This will move the ViewPlatform back a bit so the
			// objects in the scene can be viewed.
			simpleU.getViewingPlatform().setNominalViewingTransform();
			simpleU.getViewer().getView().setFieldOfView(0.26f);
			simpleU.addBranchGraph(bg);
			frame=new MainFrame(this, WineBottle.imagewidth,WineBottle.imageheight);
			isinitialized=true;
		}

	}

	public void generate(){



		Dbutil.executeQuery("delete from wineads where wineid=1;");
		Dbutil.executeQuery("insert into wineads (wineid,image,producer,winename,vintage,region,color,imagetype,labelurl) values (1,'','','"+Spider.SQLEscape(this.winename)+"',"+this.vintage+",'"+this.region+"','"+this.winecolor+"','png','"+url+"');");
		
		BranchGroup scene = createSceneGraph();
		
		bg.addChild(scene);
		

		// SimpleUniverse is a Convenience Utility class

		//simpleU.addBranchGraph(scene);
		


	}

	/////////////////////////////////////////////////
	//
	// create scene graph branch group
	//
	public BranchGroup createSceneGraph() {

		BranchGroup contentRoot = new BranchGroup();
		contentRoot.setCapability(BranchGroup.ALLOW_DETACH);
		float spin=(float)Math.random();

		// Create the transform group node and initialize it to the
		// identity. Add it to the root of the subgraph.
		TransformGroup objSpin = new TransformGroup();
		objSpin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		Label label=new Label(winename,vintage,region,price,url,ratings);
		Bottle bottle=new Bottle(label);
		bottle.glasscolor=glasscolor;
		bottle.winecolor=winecolor;
		objSpin.addChild(bottle.getShape());
		Bottle shade=new Bottle(label);
		shade.winecolor=winecolors.SHADE;
		TransformGroup tgshade = new TransformGroup();
		Transform3D transform = new Transform3D();
		Vector3f vector = new Vector3f(0.04f, .0f, -0.15f);
		transform.setTranslation(vector);
		tgshade.setTransform(transform);
		tgshade.addChild(shade.getShape());
		objSpin.addChild(tgshade);  
		objSpin.addChild(label);

		Transform3D position1 = new Transform3D();    
		position1.setRotation(new AxisAngle4d(-0.8f,-1f,-0.4f,0.8f));
		Transform3D position2 = new Transform3D();    
		position2.setRotation(new AxisAngle4d(0.4f,0f,1f,spin*0.5f));
		Alpha rotationAlpha = new Alpha(-1, 6000);
		position1.mul(position2);
		//position1.setTranslation(new Vector3d(0.0f,0.0f,0.0f));
		objSpin.setTransform(position1);
		RotationInterpolator rotator =
			new RotationInterpolator(rotationAlpha, objSpin);
		BoundingSphere bounds = new BoundingSphere(new Point3d(),3f);
		rotator.setSchedulingBounds(bounds);
		rotator.setTransformAxis(position1);
		contentRoot.addChild(objSpin);

		Back back=new Back("");
		contentRoot.addChild(back);

		AmbientLight light = new AmbientLight();
		light.setColor(new Color3f(0.5f,0.5f,0.5f));
		//light.setColor(new Color3f(0.1f,0.1f,0.1f));
		light.setInfluencingBounds(bounds);
		contentRoot.addChild(light);

		SpotLight spot1= new SpotLight();
		spot1.setInfluencingBounds(bounds);
		spot1.setPosition((float)Math.random()*0.4f-0.2f,1f,1f);
		Vector3f spotdirection1 = new Vector3f(0, -1f, -1f);
		spotdirection1.normalize();
		spot1.setDirection(spotdirection1);
		spot1.setColor(new Color3f(1f, 1f, 1f));
		contentRoot.addChild(spot1);
		SpotLight spot2=new SpotLight();
		spot2.setInfluencingBounds(bounds);
		spot2.setPosition(0f,1f,1f);
		Vector3f spotdirection2 = new Vector3f(-0.2f, -0.2f, -0.2f);
		spotdirection2.normalize();
		spot2.setDirection(spotdirection2);
		spot2.setColor(new Color3f(1.0f, 1.0f, 1.0f));
		contentRoot.addChild(spot2);


		DirectionalLight lightD1 = new DirectionalLight();
		lightD1.setInfluencingBounds(bounds);
		Vector3f direction = new Vector3f(-1.0f, -1.0f, -1.0f);
		direction.normalize();
		lightD1.setDirection(direction);
		lightD1.setColor(new Color3f(1.0f, 1.0f, 1.0f));
		//contentRoot.addChild(lightD1);

		// make background wood
		//TextureLoader myLoader = new TextureLoader( "images/wood.jpg", this );
		//ImageComponent2D myImage = myLoader.getImage( );
		Background background = new Background(1.0f, 1.0f, 1.0f);
		//background.setImage( myImage );
		//background = new Background(0.0f, 0.0f, 1.0f);
		background.setApplicationBounds(new BoundingSphere(new Point3d( ), 1 ));
		contentRoot.addChild(background);


		// Let Java 3D perform optimizations on this scene graph.

		contentRoot.compile();

		return contentRoot;
	} // end of CreateSceneGraph method of EarthApp

	// Create a simple scene and attach it to the virtual universe



	public void windowClosing(WindowEvent e){
		System.exit(0);
	}


	//  The following method allows this to be run as an application


	public static void main(String[] args) {
		ArrayList<String> ratings=new ArrayList<String>();
		ratings.add("RP");
		ratings.add("94");
		ratings.add("WS");
		ratings.add("92");
		MainFrame frame;
		WineBottle wb=new WineBottle();
		if (true){
			wb.winename="Dario Raccaro";
			wb.vintage="Vigna del Rolat 2006";
			wb.region="Tocai Friulano";
			wb.price="21,95";
			wb.glasscolor=glasscolors.GREEN;
			wb.winecolor=winecolors.WHITE;
			wb.wineid=1;
			wb.ratings=ratings;
			wb.generate();
			frame=new MainFrame(wb, WineBottle.imagewidth,WineBottle.imageheight);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			wb.canvas3D.writeJPEG=true;
			//wb.canvas3D.frame=frame;
			wb.canvas3D.repaint();
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.removeAll();
		wb=new WineBottle();
		wb.winename="Tua Rita\nRedigaffi";
		wb.vintage="2006";
		wb.region="Toscana IGT";
		wb.price="129,00";
		wb.glasscolor=glasscolors.GREEN;
		wb.winecolor=winecolors.RED;
		wb.wineid=2;
		wb.ratings=ratings;
		wb.generate();
		frame=new MainFrame(wb, WineBottle.imagewidth,WineBottle.imageheight);

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		wb.canvas3D.writeJPEG=true;
		//wb.canvas3D.frame=frame;
		wb.canvas3D.repaint();

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.removeAll();
		frame.addWindowListener(wb);
		frame.dispose();
	}
	//System.exit(0);
	//MainFrame frame=new MainFrame(new WineBottle("l"), 30, 30);


	// end of main method of EarthApp



	
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
 
	}



	
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		System.exit(0);
	}



	
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}



	
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}



	
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}



	
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}


	public void fillInfo(){
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from wines join knownwines on (wines.knownwineid=knownwines.id) where wines.id="+wineid;
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				knownwineid=rs.getInt("knownwineid");
				winename=rs.getString("name");
				vintage=rs.getString("vintage");
				region="";
				price=Webroutines.formatPrice(rs.getDouble("priceeuroex"));
				winecolor=winecolors.RED;
				glasscolor=glasscolors.GREEN;
				String color=rs.getString("type");
				if (color.contains("White")){
					winecolor=winecolors.WHITE;
					if (color.contains("Sweet")){
						winecolor=winecolors.WHITESWEET;
						glasscolor=glasscolors.WHITE;
					}

				}

			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	
	public void run() {
		bg.removeAllChildren();
		generate();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		canvas3D.writeJPEG=true;
		//canvas3D.frame=frame;
		canvas3D.repaint();
		//canvas3D.writeJPEG=false;

	}

} // end of class EarthApp
