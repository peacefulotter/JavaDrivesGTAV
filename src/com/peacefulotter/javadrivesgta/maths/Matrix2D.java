package com.peacefulotter.javadrivesgta.maths;


import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;


public class Matrix2D
{
    private final double[][] m;
    public final int rows, cols;

    public Matrix2D( int rows, int cols)
    {
        this.m = new double[rows][cols];
        this.rows = rows;
        this.cols = cols;
    }

    public Matrix2D( Matrix2D mat ) {
        this.m = Arrays.copyOf( mat.m, mat.rows );
        this.rows = mat.rows;
        this.cols = mat.cols;
    }

    public Matrix2D( double[][] m )
    {
        this.m = deepCopy( m );
        this.rows = m.length;
        this.cols = m[0].length;
    }

    public static double[][] deepCopy(double[][] original) {
        final double[][] res = new double[original.length][];
        for (int i = 0; i < original.length; i++) {
            res[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return res;
    }

    /**
     * Creates a new matrix of size (cols, rows) and apply a function 'func' to each of its elements
     * @param func: the function
     * @return a new Matrix m = func(new Matrix(rows, cols))
     */
    public static Matrix2D applyFunc( MatrixLambda func, int rows, int cols)
    {
        Matrix2D res = new Matrix2D( rows, cols );
        return res.applyFunc( func );
    }

    /**
     * Creates a new matrix of the same size as this and apply a function 'func' to each elements
     * @param func: the function
     * @return a new Matrix m = func(this)
     */
    public Matrix2D applyFunc( MatrixLambda func )
    {
        Matrix2D res = new Matrix2D( rows, cols );
        for ( int i = 0; i < rows; i++ )
        {
            for ( int j = 0; j < cols; j++ )
            {
                res.m[i][j] = func.apply( res, i, j );
            }
        }
        return res;
    }

    /**
     * Run a function 'func' to all the elements
     * @param func: the function
     */
    public void execFunc( MatrixLambda func )
    {
        for ( int i = 0; i < rows; i++ )
        {
            for ( int j = 0; j < cols; j++ )
            {
                func.apply( this, i, j );
            }
        }
    }


    public static Matrix2D genRandomDouble( int rows, int cols ) {
        Random r = new Random();
        return Matrix2D.applyFunc( (mat, i, j) -> r.nextGaussian(), rows, cols);
    }

    public static Matrix2D genRandomInt( int width, int height, int min, int max )
    {
        return Matrix2D.applyFunc( (mat, i, j) ->
                ThreadLocalRandom.current().nextInt(min, max + 1), height, width);
    }

    public Matrix2D transpose()
    {
        return Matrix2D.applyFunc( (mat, i, j) -> m[j][i], cols, rows);
    }


    public static class ElemIndices
    {
        public double elem;
        public int x, y;

        private ElemIndices( double elem, int x, int y ) { this.elem = elem; this.x = x; this.y = y; }
    }

    public ElemIndices max()
    {
        ElemIndices res = new ElemIndices( -Double.MAX_VALUE, 0, 0 );
        execFunc( (m, i, j) -> {
            if ( getAt( i, j ) > res.elem )
            {
                res.elem = getAt( i, j );
                res.x = j;
                res.y = i;
            }
            return 0;
        } );
        return res;
    }

    public Matrix2D normalize()
    {
        double mean = mean();
        double variance = variance(mean);
        double std = std(variance);
        if (mean == 0 && std == 0)
            return new Matrix2D( rows, cols );
        return applyFunc( (mat,i,j) -> (m[i][j] - mean) / std );
    }

    public Matrix2D plus( double a )
    {
        return Matrix2D.applyFunc(( mat, i, j) -> m[i][j] + a, rows, cols);
    }

    public Matrix2D sub( double a ) { return plus(-a); }

    public Matrix2D mul( double a )
    {
        return Matrix2D.applyFunc(( mat, i, j) -> m[i][j] * a, rows, cols);
    }

    public Matrix2D div( double a )
    {
        return mul( 1 / a );
    }

    public Matrix2D pow( double a )
    {
        return Matrix2D.applyFunc( (mat, i, j) -> Math.pow(m[i][j], a), rows, cols);
    }

    public Matrix2D plus( Matrix2D other )
    {
        if (rows >1 && other.rows == 1 && cols == other.cols)
        {
            return applyFunc( (mat, i, j) -> m[i][j] + other.m[0][j] );
        }
        else if ( rows != other.rows || cols != other.cols ) throw new AssertionError();
        return Matrix2D.applyFunc( (mat, i, j) -> m[i][j] + other.m[i][j], rows, cols);
    }

    public Matrix2D sub( Matrix2D other )
    {
        return plus(other.mul(-1));
    }

    private Matrix2D mulSameSize( Matrix2D other )
    {
        return applyFunc( (mat, i, j) -> m[i][j] * other.m[i][j] );
    }

    public Matrix2D mul( Matrix2D other )
    {
        if ( rows == other.rows && cols == other.cols )
            return mulSameSize( other );
        else if ( cols != other.rows ) throw new AssertionError();

        return Matrix2D.applyFunc( (mat, i, j) -> {
            double value = 0;
            for ( int k = 0; k < cols; k++ )
            {
                value += this.m[i][k] * other.m[k][j];
            }
            return value;
        }, rows, other.cols);
    }

    public Matrix2D dot( Matrix2D other )
    {
        return transpose().mul(other);
    }

    public Matrix2D selectRows(int a, int b) {
        Matrix2D res = new Matrix2D(b - a, cols );
        for ( int i = a; i < b; i++ )
        {
            for ( int j = 0; j < cols; j++ )
            {
                res.setAt( i - a, j, getAt( i, j ) );
            }
        }
        return res;
    }

    public Matrix2D subMatrix( int x, int y, int width, int height )
    {
        Matrix2D res = new Matrix2D( height, width );
        return res.applyFunc( (mat, i, j) -> this.m[y + i][x + j] );
    }

    public void subMatrix( int x, int y, int width, int height, Matrix2D src )
    {
        applyFunc( (mat, i, j) -> src.m[y + i][x + j] );
    }

    public Matrix2D shuffleRows() {
        Matrix2D mat = new Matrix2D(this);
        Collections.shuffle(Arrays.asList(mat.m));
        return mat;
    }

    public Matrix2D shuffleRows(int[] indices) {
        if ( indices.length != rows ) throw new AssertionError();
        return Matrix2D.applyFunc( (mat, i, j) -> m[indices[i]][j], rows, cols);
    }

    public double[] getRow( int i )
    {
        return m[i];
    }

    public double getAt(int i, int j)
    {
        return m[i][j];
    }

    public void setAt(int i, int j, double val)
    {
        m[i][j] = val;
    }

    public double mean()
    {
        double[] res = new double[] { 0d };
        applyFunc( (m, i, j) -> {
            res[0] += getAt( i, j );
            return 0;
        } );
        return res[0] / (rows * cols);
    }

    public double variance() { return variance(mean()); }

    public double variance(double mean)
    {
        double[] res = new double[] { 0d };
        applyFunc( (m, i, j) -> {
            res[0] += Math.pow( getAt( i, j ) - mean, 2);
            return 0;
        } );
        return res[0] / (rows * cols);
    }

    public double std() { return std(variance()); }

    public double std( double variance ) {
        return Math.sqrt( variance );
    }

    public String shape()
    {
        StringJoiner sj = new StringJoiner( ", ", "(", ")" );
        sj.add( String.valueOf( rows ) );
        sj.add( String.valueOf( cols ) );
        return sj.toString();
    }

    @Override
    public String toString()
    {
        StringJoiner main = new StringJoiner("\n", "[", "]");
        for ( int i = 0; i < rows; i++ )
        {
            StringJoiner sj = new StringJoiner(",\t", "[", "]");
            for ( int j = 0; j < cols; j++ )
            {
                sj.add( String.valueOf( m[i][j] ) );
            }
            main.add( sj.toString() );
        }
        return main.toString();
    }
}
