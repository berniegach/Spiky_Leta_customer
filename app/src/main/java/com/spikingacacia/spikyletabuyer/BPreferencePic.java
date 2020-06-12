package com.spikingacacia.spikyletabuyer;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceViewHolder;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.spikingacacia.spikyletabuyer.util.VolleyMultipartRequest;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static com.spikingacacia.spikyletabuyer.LoginA.serverAccount;



/**
 * Created by $USER_NAME on 9/20/2018.
 **/
public class BPreferencePic extends Preference
{
    private static final int PERMISSION_REQUEST_INTERNET=2;
    private static String url_upload_profile_pic= LoginA.base_url+"upload_profile_pic_b.php";
    public static NetworkImageView imageView;
    public static TextView textView;
    private static Context context;
    private static String TAG_SUCCESS="success";
    private static String TAG_MESSAGE="message";
    private FragmentManager fragmentManager;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public BPreferencePic(Context context)
    {
        super(context);
        setLayoutResource(R.layout.bsettings_profilepic);
        this.context=context;
        fragmentManager=((AppCompatActivity)context).getFragmentManager();
    }

    public BPreferencePic(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        setLayoutResource(R.layout.bsettings_profilepic);
        this.context=context;
        fragmentManager=((AppCompatActivity)context).getFragmentManager();
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
    }
    public BPreferencePic(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context,attrs,defStyleAttr);
        setLayoutResource(R.layout.bsettings_profilepic);
        this.context=context;
        fragmentManager=((AppCompatActivity)context).getFragmentManager();
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();    }
    @Override
    public void onBindViewHolder(PreferenceViewHolder view)
    {
        super.onBindViewHolder(view);
        String image_url= LoginA.base_url+"src/buyers_pics/";
        imageView=(NetworkImageView)view.findViewById(R.id.image);
        //get the profile pic
        // thumbnail image
        String url=image_url+String.valueOf(serverAccount.getId())+'_'+String.valueOf(serverAccount.getImageType());
        imageView.setImageUrl(url, imageLoader);
        view.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final FragmentManager fragmentManager=((AppCompatActivity)context).getFragmentManager();
                Fragment fragment= GetPicture.newInstance();

                if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    fragmentManager.beginTransaction().add(fragment,"AB").commit();
                    fragmentManager.executePendingTransactions();
                    Intent intent=new Intent();
                    //show only images
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpeg"});
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    fragment.startActivityForResult(Intent.createChooser(intent,"Select profile Image in jpg format"),1);
                    notifyChanged();
                }
                else
                {
                    ActivityCompat.requestPermissions((Activity) getContext(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_INTERNET);
                }


            }
        });

    }

    public static class GetPicture extends Fragment
    {
        public static GetPicture newInstance()
        {
            GetPicture getPicture=new GetPicture();
            return getPicture;

        }

        @Override
        public void onActivityResult(final int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode,resultCode,data);
            if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null)
            {
                final Uri uri = data.getData();
                try
                {

                    final String path = getPath(uri);
                    Log.d("path",path);

                if (true)
                {
                    final Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                    imageView.setImageBitmap(bitmap);
                    Toast.makeText(context, "Profile pic changed", Toast.LENGTH_SHORT).show();
                    //upload
                    if (path == null)
                    {
                        Log.e("upload cert","its null");
                    }
                    else
                    {
                        //Uploading code
                        Log.d("uploading","1");
                        try
                        {
                            uploadBitmap(bitmap);

                        }
                        catch (Exception e)
                        {
                            Log.d("uploading","2");
                            e.printStackTrace();
                        }
                    }
                }
                }
                catch (Exception e)
                {
                    Log.e("bitmap", "" + e.getMessage());
                }
            }
            getFragmentManager().beginTransaction().remove(this).commit();
        }
        private String getPath(Uri uri)
        {
            if(uri==null)
                return null;
            String res=null;

            if (DocumentsContract.isDocumentUri(getActivity(), uri))
            {
                //emulator
                String[] path = uri.getPath().split(":");
                res = path[1];
                Log.i("debinf ProdAct", "Real file path on Emulator: "+res);
            }
            else {
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(uri, proj, null, null, null);
                if (cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    res = cursor.getString(column_index);
                }
                cursor.close();
            }
            return res;
        }
        private void uploadBitmap(final Bitmap bitmap) {

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url_upload_profile_pic,
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response)
                        {
                            int statusCode = response.statusCode;
                            Log.d("gdjhsdj",""+statusCode);
                            serverAccount.setImageType(".png");
                            SettingsActivity.settingsChanged = true;
                            Toast.makeText(context, "Profile pic changed", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("GotError",""+error.getMessage());
                        }
                    }) {


                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    long imagename = System.currentTimeMillis();
                    params.put("png", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                    return params;
                }


                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String , String >params = new HashMap<>();
                    params.put("name", "name"); //Adding text parameter to the request
                    params.put("id",String.valueOf(serverAccount.getId()));
                    return params;
                }
            };

            //adding the request to volley
            Volley.newRequestQueue(context).add(volleyMultipartRequest);
        }
        public byte[] getFileDataFromDrawable(Bitmap bitmap) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }
}
