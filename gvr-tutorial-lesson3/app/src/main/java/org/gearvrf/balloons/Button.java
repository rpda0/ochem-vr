package org.gearvrf.balloons;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.joml.Vector3f;

/**
 * Created by jinhee.k on 7/30/2016.
 */
public class Button extends GVRBehavior {
    public  float            Velocity;
    public  Vector3f         Direction;
    public  float            Distance;
    private Vector3f        mStartPos = new Vector3f(0, 0, 0);
    private Vector3f        mCurPos = new Vector3f(0, 0, 0);

    Button(GVRContext gvrContext, float velocity, Vector3f direction)
    {
        super(gvrContext);
        Velocity = velocity;
        Direction = direction;


    }

    double spring(double factor, double progress) {
        double x = progress;
        progress = Math.pow (2.0, -10.0*x)
                * Math.sin((x - factor / 4.0) * (2.0 * Math.PI)/factor)
                + 1.0;
        return progress;
    }
}
