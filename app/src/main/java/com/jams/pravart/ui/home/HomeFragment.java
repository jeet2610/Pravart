package com.jams.pravart.ui.home;

import static com.firebase.ui.auth.ui.email.CheckEmailFragment.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jams.pravart.MainActivity;
import com.jams.pravart.R;
import com.jams.pravart.ml.Model;
import com.jams.pravart.model.report_model;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {


    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int ACCESS_FINE_LOCATION = 34;



    public String result = "camera ON ";

    private HomeViewModel homeViewModel;

    public Button emergencyButton;

    public ImageView imageView;

    int imageSize = 224;

    public TextView accident_text;

    private FirebaseFirestore db;

    Bitmap bmp;

    URL url;

    String Location = "";
    private LocationRequest locationRequest;

    LocationManager locationManager;

    LocationListener locationListener;

    String lat;

    protected String latitude, longitude;

    Context context;

    int maxPos = 0;
    float maxConfidence = 0;
    String[] classes = {"Non - Accident", "Accident"};
    String image_url;






    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("images");
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();


    byte[] datab;

    String photoStringLink;





    public View  onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)   {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);


        //      final TextView textView = root.findViewById(R.id.text_home);


        emergencyButton = root.findViewById(R.id.Emergency_Button);
        imageView = root.findViewById(R.id.imageView);
        accident_text = root.findViewById(R.id.Accident_text);


        db = FirebaseFirestore.getInstance();


        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,ACCESS_FINE_LOCATION);




            }
        });

        return root;
    }


    public void checkPermission(String permission, int requestCode)
    {
        if(requestCode == CAMERA_PERMISSION_CODE) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED) {

                // Requesting the permission
                ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
            } else {
                Toast.makeText(getActivity(), "Permission already granted", Toast.LENGTH_SHORT).show();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                getActivity().startActivityFromFragment(HomeFragment.this, cameraIntent, CAMERA_REQUEST);
            }


        }
        if (requestCode==ACCESS_FINE_LOCATION){
            if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED) {

                // Requesting the permission
                ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
            } else {
                Log.d("Allowed", "checkPermission: location");
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == CAMERA_REQUEST) {
                if (resultCode == Activity.RESULT_OK && data != null) {

                    bmp = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    datab  = stream.toByteArray();

                    imageView.setImageBitmap(bmp);


                    bmp = Bitmap.createScaledBitmap(bmp, imageSize, imageSize, false);


                    imageclassify(bmp);


                    Toast.makeText(this.getActivity(), "camera is on ", Toast.LENGTH_LONG).show();

                }
            }
        } catch (Exception e) {
            Log.d("eerror", "onActivityResult: "+e.getLocalizedMessage());
            Toast.makeText(this.getActivity(), e + "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }






    //image tensorflow


    public void imageclassify(Bitmap bitmap) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Log.d("Time", "imageclassify: "+timestamp.toString().trim());
        StorageReference imagesRef = storageReference.child(timestamp.toString().trim());
        try {
            getCurrentLocation();
            Model model = Model.newInstance(this.requireActivity());


            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }


            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            accident_text.setText(classes[maxPos]);




            if(classes[maxPos].equals("Accident")){


                UploadTask uploadTask = imagesRef.putBytes(datab);
                uploadTask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    Log.d("dimage is not added ", " image is not added ");
                }).addOnSuccessListener(taskSnapshot -> {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Log.d("dimage is added ", " image is added ");


                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            photoStringLink = uri.toString();
                            Log.d("image url "," "+photoStringLink);

                            CollectionReference dbCourses = db.collection("report");
                            Log.d("lcoation", "imageclassify: "+Location);
                            if(photoStringLink  !=null && Location != null) {
                                report_model rm = new report_model(photoStringLink, Location);


                                dbCourses.add(rm).addOnSuccessListener(documentReference -> {
                                    // after the data addition is successful
                                    // we are displaying a success toast message.

                                    Log.d("firestore final  ", "onSuccess: data is added");


                                    Toast.makeText(getActivity(), "Your data has been added to Firebase Firestore", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    // this method is called when the data addition process is failed.
                                    // displaying a toast message when data addition is failed.
                                    Log.d("firestore error ", "ONFAILURE: data is NOT ADDED" + e.getLocalizedMessage());
                                    Toast.makeText(getActivity(), "Fail to add data \n" + e, Toast.LENGTH_SHORT).show();
                                });
                            }else{
                                Toast.makeText(getContext(), "noData found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                });




            }



            // Releases model resources if no longer used.
            model.close();
        } catch (Exception e) {
            Log.d("Error", "imageclassify: "+e.getLocalizedMessage());
        }

    }

    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(getActivity())
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(getActivity())
                                            .removeLocationUpdates(this);

                                    if (locationResult.getLocations().size() >0){

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        Log.d("map", "onLocationResult: "+locationResult.getLocations());
                                        Location = "https://maps.google.com/?q="+latitude+","+longitude;
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getActivity())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(getContext(), "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(getActivity(), 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }


}




