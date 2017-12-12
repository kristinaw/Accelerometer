package com.example.kristinawettainen.myapplication;

        import java.io.BufferedWriter;
        import java.io.File;
        import java.io.FileWriter;
        import java.io.IOException;

        import android.support.v7.app.ActionBarActivity;
        import android.content.Intent;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.Bundle;
        import android.os.Environment;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;

        import android.widget.TextView;



public class Start_Activity extends ActionBarActivity implements SensorEventListener {

    private SensorManager mSensorManager; //SensorManager lets you access the device's sensors.
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagnetometer;
    private Sensor mGravitation;
    String sdcard = Environment.getExternalStorageDirectory().getPath();
    String name = sdcard + "/geomagnetic.csv";
    File namefile = new File(name);
    BufferedWriter out;
    Float azimut;  // View to draw a compass
    float x,y,z;
    float [] mGeomagnetic;
    float [] mAcc;
    float [] mGyro;
    float [] mGravity;
    float Rot [];
    float I [] ;
    float mOrientation [];
    float mRotvector [];
    String resultX;
    String resultRot;
    String resultGeo;

    /*
     * time smoothing constant for low-pass filter
     * 0 <= alpha <= 1 ; a smaller value basically means more smoothing
     * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    static final float ALPHA = 0.15f;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGravitation = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        Rot = new float [9];
        I = new float [9];
        mAcc = new float[3];
        mOrientation = new float[3];
        mRotvector = new float[3];
        mGravity = new float[3];
        mGyro = new float[3];
        resultX = new String("");
        resultRot = new String("");
        resultGeo = new String("");



        try {
            out = new BufferedWriter(new FileWriter(namefile));
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetometer,SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGravitation,SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroscope,SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
    }


	/* (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {



        //Declare the widgets in the UI
        TextView tvX = (TextView)findViewById(R.id.x_axis);
        TextView tvY = (TextView)findViewById(R.id.y_axis);
        TextView tvZ = (TextView)findViewById(R.id.z_axis);
        TextView tvAzimut = (TextView)findViewById(R.id.azimut);
//		TextView tvPitch = (TextView)findViewById(R.id.pitch);
//		TextView tvRoll= (TextView)findViewById(R.id.roll);

        synchronized (this) {

            switch (event.sensor.getType()){

                case Sensor.TYPE_LINEAR_ACCELERATION:

                    mAcc = event.values; //Acceleration including Gx on the x-axis
                    // mAcc = lowPass(mAcc,mAcc);

                    tvX.setText(Float.toString(mAcc[0]));
                    tvY.setText(Float.toString(mAcc[1]));
                    tvZ.setText(Float.toString(mAcc[2]));

                    break;


                case Sensor.TYPE_GYROSCOPE:

                    mGyro = event.values;

                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:

                    mGeomagnetic = event.values;
                    //mGeomagnetic = lowPass(mGeomagnetic,mGeomagnetic);

                    break;

                case Sensor.TYPE_GRAVITY:

                    mGravity = event.values;
                    //mGravity = lowPass(mGravity,mGravity);

                    break;


            }

        }

        if (mGravity != null && mGeomagnetic != null && mAcc != null && mGyro!=null ){

            boolean success = SensorManager.getRotationMatrix(Rot, I, mGravity, mGeomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(Rot, mOrientation);

                //Declare the widgets in the UI
                tvAzimut.setText(Float.toString(mOrientation[0]));
                //tvPitch.setText(Float.toString(mOrientation[1]));
                //tvRoll.setText(Float.toString(mOrientation[2]));


                resultX = Float.toString(mAcc[0]) + ";"
                        + Float.toString(mAcc[1]) + ";"
                        + Float.toString(mAcc[2]) + ";";

                resultRot = Float.toString(Rot[0]) + ";"
                        + Float.toString(Rot[1]) + ";"
                        + Float.toString(Rot[2]) + ";"
                        + Float.toString(Rot[3]) + ";"
                        + Float.toString(Rot[4]) + ";"
                        + Float.toString(Rot[5]) + ";"
                        + Float.toString(Rot[6]) + ";"
                        + Float.toString(Rot[7]) + ";"
                        + Float.toString(Rot[8]) + ";"
                        + Float.toString(I[0]) + ";"
                        + Float.toString(I[1]) + ";"
                        + Float.toString(I[2]) + ";"
                        + Float.toString(I[3]) + ";"
                        + Float.toString(I[4]) + ";"
                        + Float.toString(I[5]) + ";"
                        + Float.toString(I[6]) + ";"
                        + Float.toString(I[7]) + ";"
                        + Float.toString(I[8]) + ";"
                        + Float.toString(mGyro[0]) + ";"
                        + Float.toString(mGyro[1]) + ";"
                        + Float.toString(mGyro[2]) + ";";

                resultGeo = resultX + resultRot + Float.toString(mOrientation[0]) + ";"
                        + Float.toString(mOrientation[1]) + ";"
                        + Float.toString(mOrientation[2]);

                // write rotation values to file

                try
                {
                    out.append(resultGeo);
                    out.newLine();

                }
                catch (IOException e)
                {
                    resultX = new String("");
                }

                resultX = new String("");
                resultRot = new String("");

            }
        }

    }

    public void stopFunc(View v) throws IOException {

        // write rotation values to file

        out.close();
        Intent i = new Intent(Start_Activity.this, MainActivity.class);
        startActivity(i);
    }

	/*
	public void addTextToFile(String text) {
        File logFile = new File("sdcard/" + "MyFile.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	*/




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }
}
