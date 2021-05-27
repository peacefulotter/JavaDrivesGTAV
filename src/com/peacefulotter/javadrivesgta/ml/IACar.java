package com.peacefulotter.javadrivesgta.ml;


import com.peacefulotter.javadrivesgta.maths.Matrix2D;
import com.peacefulotter.javadrivesgta.ml.activation.ActivationFunc;
import com.peacefulotter.javadrivesgta.ml.activation.Activations;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.peacefulotter.javadrivesgta.utils.Settings.CAPTURE_HEIGHT;
import static com.peacefulotter.javadrivesgta.utils.Settings.CAPTURE_WIDTH;

public class IACar
{
    // Neural Network specifications (hyper parameters)
    public static final int[] DIMENSIONS = new int[] {CAPTURE_WIDTH * CAPTURE_HEIGHT, 10, 2};
    private static final ActivationFunc[] ACTIVATIONS = new ActivationFunc[] {
            Activations.ReLU, Activations.HyperTan
    };

    private NeuralNetwork nn;

    public IACar()
    {
        this.nn = new NeuralNetwork( DIMENSIONS, ACTIVATIONS );
    }

    public IACar( NeuralNetwork nn )
    {
        this.nn = nn;
    }

    /**
     * Simulates the NeuralNetwork
     * @return the prediction of the neural network
     */
    public Matrix2D simulate()
    {
        Matrix2D data = new Matrix2D( 1, DIMENSIONS[0] );
        // data.setAt( 0, nbArrows + 2, angle );
        return nn.predict( data.normalize() );
    }

    public void setNN( NeuralNetwork nn )
    {
        this.nn = nn;
    }

    public NeuralNetwork applyNNFunction( Function<Matrix2D, Matrix2D> func )
    {
        return nn.applyFunction( func );
    }

    public NeuralNetwork applyNNFunction( BiFunction<Matrix2D, Matrix2D, Matrix2D> func, IACar other )
    {
        return nn.applyFunction( func, other.nn );
    }

    /*public void trainIA()
    {
        HashMap<String, Matrix2D> hm = new Loader().loadDrivingData( DIMENSIONS[0], DIMENSIONS[DIMENSIONS.length - 1]);
        Matrix2D X = hm.get( "X" );
        Matrix2D Y = hm.get( "Y" );

        if ( X.cols != DIMENSIONS[ 0 ] ) throw new AssertionError();

        nn.train( X, Y, Loss.MSE, 1e-4, 250, 1, 10 );
        // nextGeneration( new ArrayList<>(1) { { add(0); } }, population );
    }*/

    public NeuralNetwork getCopyNN() { return new NeuralNetwork( nn ); }
}
