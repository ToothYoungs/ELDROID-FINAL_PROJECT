package com.example.eldroid_final_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Dashboard extends AppCompatActivity {

    TextView tv_back;
    SearchView sv_search;
    RecyclerView recyclerView_searches;
    Button btn_add;
    List<Tech> arrUsers, arr;
    List<String> arrKey;
    AdapterTech adapterTech, adapter;
    FirebaseAuth fAuth;
    FirebaseUser user;
    DatabaseReference techDB;

    final int PICK_IMAGE = 0;
    String category;
    String userKey, keyID;
    Uri imageUri;
    EditText et_firstName, et_lastName, et_contactNumber, et_username;
    ImageView iv_profile_photo;
    TextView tv_uploadPhoto;
    Button btn_signUp;

    StorageReference userStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        user = FirebaseAuth.getInstance().getCurrentUser();
        techDB = FirebaseDatabase.getInstance().getReference("Tech");
        userKey = user.getUid();

        String userID = user.getUid();
        userStorage = FirebaseStorage.getInstance().getReference("Tech").child(userID);


        tv_back = findViewById(R.id.tv_back);
        sv_search = findViewById(R.id.sv_search);
        recyclerView_searches = findViewById(R.id.recyclerView_searches);
        btn_add = findViewById(R.id.btn_add);

        et_firstName = findViewById(R.id.et_firstName);
        et_lastName = findViewById(R.id.et_lastName);
        et_contactNumber = findViewById(R.id.et_contactNumber);
        et_username = findViewById(R.id.et_username);
        btn_signUp = findViewById(R.id.btn_signUp);
        tv_uploadPhoto = findViewById(R.id.tv_uploadPhoto);
        iv_profile_photo = findViewById(R.id.iv_profile_photo);

        recyclerView_searches.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView_searches.setLayoutManager(linearLayoutManager);

        arrUsers = new ArrayList<>();
        arrKey = new ArrayList<>();
        adapterTech = new AdapterTech(arrUsers);
        recyclerView_searches.setAdapter(adapterTech);

        retrieve();
        click();
    }

    private void click() {

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new SweetAlertDialog(view.getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Warning")
                        .setCancelText("Back")
                        .setConfirmButton("Sign out", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                fAuth.getInstance().signOut();
                                Intent intent = new Intent(Dashboard.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setContentText("Proceed with sign out?")
                        .show();


            }
        });

//        btn_add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Dashboard.this, Dashboard.class);
//                //intent.putExtra("category", "add");
//                startActivity(intent);
//            }
//        });

        adapterTech.setOnItemClickListener(new AdapterTech.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                arrUsers.get(position);

                keyID = arrKey.get(position);
                editData(keyID);
                category = "edit";
//                Intent intent = new Intent(Dashboard.this, Dashboard.class);
//                intent.putExtra("category", "edit");
//                intent.putExtra("user id", keyID);
//                startActivity(intent);

            }
        });

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(category != null) {
                    if (category.equals("edit")) {
                        if (imageUri == null)
                        {
                            String firstName = et_firstName.getText().toString();
                            String lastName = et_lastName.getText().toString();
                            String username = et_username.getText().toString();
                            String contactNum = et_contactNumber.getText().toString();


                            if (TextUtils.isEmpty(firstName))
                            {
                                et_firstName.setError("This field is required");
                            }
                            else if (TextUtils.isEmpty(lastName))
                            {
                                et_lastName.setError("This field is required");
                            }
                            else if (TextUtils.isEmpty(username) )
                            {
                                et_username.setError("This field is required");
                            }
                            else if (TextUtils.isEmpty(contactNum))
                            {
                                et_contactNumber.setError("This field is required");
                            }
                            else if (contactNum.length() != 11)
                            {
                                et_contactNumber.setError("Contact number must be 11 digit");
                            }
                            else
                            {
                                final ProgressDialog progressDialog = new ProgressDialog(Dashboard.this);
                                progressDialog.setTitle("Updating...");
                                progressDialog.show();

                                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                hashMap.put("firstName", firstName);
                                hashMap.put("lastName", lastName);
                                hashMap.put("contactNum", contactNum);
                                hashMap.put("username", username);

                                techDB.child(keyID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            progressDialog.dismiss();
                                            Intent intent = new Intent(Dashboard.this, MainActivity.class);
                                            startActivity(intent);
                                            Toast.makeText(Dashboard.this, "User Updated", Toast.LENGTH_LONG).show();

                                        } else {
                                            Toast.makeText(Dashboard.this, "Update Failed ", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        }
                        else
                        {
                            String firstName = et_firstName.getText().toString();
                            String lastName = et_lastName.getText().toString();
                            String username = et_username.getText().toString();
                            String password = "";
                            String contactNum = et_contactNumber.getText().toString();
                            String ratings = "0";

                            if (imageUri == null)
                            {
                                Toast.makeText(Dashboard.this, "Profile photo is required", Toast.LENGTH_SHORT).show();
                            }
                            else if (TextUtils.isEmpty(firstName))
                            {
                                et_firstName.setError("This field is required");
                            }
                            else if (TextUtils.isEmpty(lastName))
                            {
                                et_lastName.setError("This field is required");
                            }
                            else if (TextUtils.isEmpty(username))
                            {
                                et_username.setError("This field is required");
                            }
                            else if (TextUtils.isEmpty(contactNum) )
                            {
                                et_contactNumber.setError("This field is required");
                            }
                            else if (contactNum.length() != 11)
                            {
                                et_contactNumber.setError("Contact number must be 11 digit");
                            }
                            else
                            {
                                final ProgressDialog progressDialog = new ProgressDialog(Dashboard.this);
                                progressDialog.setTitle("Updating...");
                                progressDialog.show();

                                StorageReference fileReference = userStorage.child(imageUri.getLastPathSegment());
                                String imageName = imageUri.getLastPathSegment();

                                fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                final String imageURL = uri.toString();

                                                String uid = "";
                                                Tech users = new Tech(uid, firstName, lastName, contactNum, username, password, imageName, imageURL, ratings);

                                                techDB.child(keyID).setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            progressDialog.dismiss();
                                                            Intent intent = new Intent(Dashboard.this, Dashboard.class);
                                                            startActivity(intent);
                                                            Toast.makeText(Dashboard.this, "User Created", Toast.LENGTH_LONG).show();

                                                        } else {
                                                            Toast.makeText(Dashboard.this, "Creation Failed ", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Dashboard.this, "Failed: ", Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                    }
                                });


                            }
                        }


                    }
                }
                else
                {
                    String firstName = et_firstName.getText().toString();
                    String lastName = et_lastName.getText().toString();
                    String username = et_username.getText().toString();
                    String password = "";
                    String contactNum = et_contactNumber.getText().toString();
                    String ratings = "0";

                    if (imageUri == null)
                    {
                        Toast.makeText(Dashboard.this, "Profile photo is required", Toast.LENGTH_SHORT).show();
                    }
                    else if (TextUtils.isEmpty(firstName))
                    {
                        et_firstName.setError("This field is required");
                    }
                    else if (TextUtils.isEmpty(lastName))
                    {
                        et_lastName.setError("This field is required");
                    }
                    else if (TextUtils.isEmpty(username) )
                    {
                        et_username.setError("This field is required");
                    }
                    else if (TextUtils.isEmpty(contactNum))
                    {
                        et_contactNumber.setError("This field is required");
                    }
                    else if (contactNum.length() != 11)
                    {
                        et_contactNumber.setError("Contact number must be 11 digit");
                    }
                    else
                    {
                        final ProgressDialog progressDialog = new ProgressDialog(Dashboard.this);
                        progressDialog.setTitle("Creating account");
                        progressDialog.show();

                        StorageReference fileReference = userStorage.child(imageUri.getLastPathSegment());
                        String imageName = imageUri.getLastPathSegment();

                        fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String imageURL = uri.toString();

                                        String uid = "";
                                        Tech users = new Tech(uid, firstName, lastName, contactNum, username, password, imageName, imageURL, ratings);

                                        techDB.push().setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    progressDialog.dismiss();
                                                    Intent intent = new Intent(Dashboard.this, MainActivity.class);
                                                    startActivity(intent);
                                                    Toast.makeText(Dashboard.this, "User Created", Toast.LENGTH_LONG).show();

                                                } else {
                                                    Toast.makeText(Dashboard.this, "Creation Failed ", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Dashboard.this, "Failed: ", Toast.LENGTH_LONG).show();
                                            }
                                        });

                            }
                        });


                    }
                }

            }
        });

        tv_uploadPhoto.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                boolean pick = true;
                if (pick == true){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else
                        getImage();

                }else{
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else
                        getImage();
                }
            }
        });

    }

    private void editData(String keyID) {
        techDB.child(keyID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    Tech users = snapshot.getValue(Tech.class);

                    String imageUrl = users.imageUrl;
                    String firstName = users.firstName;
                    String lastName = users.lastName;
                    String contactNum = users.contactNum;
                    String email = users.email;

                    Picasso.get()
                            .load(imageUrl)
                            .into(iv_profile_photo);

                    et_firstName.setText(firstName);
                    et_lastName.setText(lastName);
                    et_username.setText(email);
                    et_contactNumber.setText(contactNum);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieve() {
        if(techDB != null)
        {
            techDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        Tech users = dataSnapshot.getValue(Tech.class);

                        String keyID = dataSnapshot.getKey().toString();
                        String userID = user.getUid();
                        if(users.userId.equals(userID))
                        {
                            continue;
                        }

                        arrUsers.add(users);
                        arrKey.add(keyID);
                    }
                    adapterTech.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        if(sv_search != null)
        {
            sv_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    search(s);
                    return false;
                }
            });

        }
    }

    private void search(String s) {
        arr = new ArrayList<>();
        for(Tech object : arrUsers)
        {
            if(object.getLastName().toLowerCase().contains(s.toLowerCase()))
            {
                arr.add(object);
            }

            adapter = new AdapterTech(arr);
            recyclerView_searches.setAdapter(adapter);
        }

        adapter.setOnItemClickListener(new AdapterTech.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                arr.get(position);
                Query query = techDB
                        .orderByChild("firstName")
                        .equalTo(arr.get(position).firstName);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {


                            String keyID = dataSnapshot.getKey().toString();
                            editData(keyID);
//                            Intent intent = new Intent(Dashboard.this, Dashboard.class);
//                            intent.putExtra("category", "edit");
//                            intent.putExtra("user id", keyID);
//                            startActivity(intent);
                        }
                        adapterTech.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });



    }

    private void getImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE){
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();

                try{
                    Picasso.get().load(imageUri)
                            .into(iv_profile_photo);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }



        }
    }

    // validate permissions
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    private boolean checkStoragePermission() {
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        return res2;
    }

    private boolean checkCameraPermission() {
        boolean res1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        return res1 && res2;
    }
}