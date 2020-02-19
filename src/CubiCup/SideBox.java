package CubiCup;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class SideBox {

    Box boxXU;
    Box boxXD;
    Box boxYU;
    Box boxYD;
    Box boxZU;
    Box boxZD;

    double x;
    double y;
    double z;

    public SideBox( double x, double y, double z, double BoxSideLength, Color color) {

        this.x = x;
        this.y = y;
        this.z = z;

        boxXU = new Box(2,BoxSideLength,BoxSideLength);
        boxXD = new Box(2,BoxSideLength,BoxSideLength);
        boxYU = new Box(BoxSideLength,2,BoxSideLength);
        boxYD = new Box(BoxSideLength,2,BoxSideLength);
        boxZU = new Box(BoxSideLength,BoxSideLength,2);
        boxZD = new Box(BoxSideLength,BoxSideLength,2);

        boxXU.setTranslateX(x-BoxSideLength/2);
        boxXU.setTranslateY(y);
        boxXU.setTranslateZ(z);

        boxXD.setTranslateX(x+BoxSideLength/2);
        boxXD.setTranslateY(y);
        boxXD.setTranslateZ(z);

        boxYU.setTranslateX(x);
        boxYU.setTranslateY(y-BoxSideLength/2);
        boxYU.setTranslateZ(z);

        boxYD.setTranslateX(x);
        boxYD.setTranslateY(y+BoxSideLength/2);
        boxYD.setTranslateZ(z);

        boxZU.setTranslateX(x);
        boxZU.setTranslateY(y);
        boxZU.setTranslateZ(z-BoxSideLength/2);

        boxZD.setTranslateX(x);
        boxZD.setTranslateY(y);
        boxZD.setTranslateZ(z+BoxSideLength/2);


        final PhongMaterial mat = new PhongMaterial();
        if( color == Color.TAN ) {
            mat.setDiffuseMap(new Image(getClass().getResourceAsStream("wood.jpg")));
        } else if( color == Color.GREEN ) {
            mat.setDiffuseMap(new Image(getClass().getResourceAsStream("green_wood.jpg")));
        } else if( color == Color.BLUE ) {
            mat.setDiffuseMap(new Image(getClass().getResourceAsStream("blue_wood.jpg")));
        } else {
            mat.setDiffuseColor(color);
        }

        boxXU.setMaterial(mat);
        boxXD.setMaterial(mat);
        boxYU.setMaterial(mat);
        boxYD.setMaterial(mat);
        boxZU.setMaterial(mat);
        boxZD.setMaterial(mat);

    }

    public void addToGroup( Group group ) {

        group.getChildren().addAll( boxXU, boxXD, boxYU, boxYD, boxZU,boxZD );

    }

}
