package CubiCup;

import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import java.lang.Math;


public class GameDisplay {

    final Group root = new Group();

    final PerspectiveCamera camera = new PerspectiveCamera(true);

    Xform cameraXform = new Xform();

    private static final double ROTATION_SPEED = .1;

    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;

    private static final double AXIS_LENGTH = 250.0;

    SubScene displayScene;

    private PointLight pointLight;
    private AmbientLight ambientLight;

    public GameDisplay( int gameSize ) {

        buildCamera(gameSize);
        buildLighting();
        //buildAxes();

        displayScene = new SubScene( root, 500, 500, true, SceneAntialiasing.BALANCED);
        displayScene.setFill(Color.GREY);
        displayScene.setCamera(camera);

        handleMouse(displayScene);

    }

    public Group getRoot() {
        return root;
    }

    public void addGameToPane( Pane pane ) {

        pane.getChildren().add( displayScene );

        displayScene.widthProperty().bind( pane.widthProperty() );
        displayScene.heightProperty().bind( pane.heightProperty() );
    }

    private void buildCamera( int gameSize ) {
        root.getChildren().add(camera);

        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(camera);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-200.0 - 60.0 * ((double)gameSize - 1.0) );

        cameraXform.rx.setAngle(-87.6);
        cameraXform.ry.setAngle(210.8);
        cameraXform.rz.setAngle(-13.8);


    }

    private void buildLighting() {

        double pointBrightneess = 0.6;
        double ambientBrightneess = 0.3;

        pointLight = new PointLight();
        ambientLight = new AmbientLight();

        pointLight.setTranslateX(-100);
        pointLight.setTranslateY(-100);
        pointLight.setTranslateZ(-100);

        setPointBrightness(pointBrightneess);
        setAmbientBrightness(ambientBrightneess);

        root.getChildren().addAll(pointLight,ambientLight);
    }

    public void setPointBrightness( double brightness ) {
        pointLight.setColor(Color.color(brightness,brightness,brightness));
    }

    public void setAmbientBrightness( double brightness ) {
        ambientLight.setColor(Color.color(brightness,brightness,brightness));
    }

    private void buildAxes() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(AXIS_LENGTH, 1, 1);
        final Box yAxis = new Box(1, AXIS_LENGTH, 1);
        final Box zAxis = new Box(1, 1, AXIS_LENGTH);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        root.getChildren().addAll(xAxis, yAxis, zAxis);
        root.setVisible(true);
    }

    private void handleMouse(SubScene scene) {

        scene.setOnMousePressed(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });

        scene.setOnMouseDragged(me -> {

            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            if (me.isPrimaryButtonDown()) {

                if (cameraXform.rx.getAngle() > -120.0 && cameraXform.rx.getAngle() < -50 ||
                        cameraXform.rx.getAngle() <= -120.0 && Math.signum(mouseDeltaY) > 0 ||
                        cameraXform.rx.getAngle() >= -50.0 && Math.signum(mouseDeltaY) < 0) {
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * ROTATION_SPEED);
                }
            }

            /*if( me.isPrimaryButtonDown() ) {
                cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * ROTATION_SPEED);
            }

            if( me.isMiddleButtonDown() ) {
                cameraXform.ry.setAngle(cameraXform.ry.getAngle() + mouseDeltaY * ROTATION_SPEED);
            }

            if( me.isSecondaryButtonDown() ) {
                cameraXform.rz.setAngle(cameraXform.rz.getAngle() + mouseDeltaY * ROTATION_SPEED);
            }*/

            //System.out.println( Math.signum(mouseDeltaY) );
            //System.out.println( cameraXform.rx.getAngle() + " : " + cameraXform.ry.getAngle() + " : " + cameraXform.rz.getAngle());
        });

        scene.setOnScroll((ScrollEvent event) -> {

            // Adjust the zoom factor as per your requirement
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();

            if (deltaY < 0){
                zoomFactor = 2.0 - zoomFactor;
            }

            cameraXform.getTransforms().add( new Scale( zoomFactor, zoomFactor, zoomFactor ) );

        });
    }

    public static class Xform extends Group {

        public Rotate rx = new Rotate(0.0,Rotate.X_AXIS);
        public Rotate ry = new Rotate(0.0, Rotate.Y_AXIS);
        public Rotate rz = new Rotate( 0.0, Rotate.Z_AXIS);

        public Xform() {
            getTransforms().addAll( rz, ry, rx );
        }
    }

}
