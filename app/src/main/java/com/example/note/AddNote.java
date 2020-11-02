package com.example.note;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddNote extends AppCompatActivity {
    FirebaseFirestore fStore;
    EditText noteTitle,noteContent;
    ProgressBar progressBarSave;
    FirebaseAuth fAuth;
    FirebaseUser user;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore=FirebaseFirestore.getInstance();
        fAuth= FirebaseAuth.getInstance();
        user=fAuth.getCurrentUser();

        noteTitle=findViewById(R.id.addNoteTitle);
        noteContent=findViewById(R.id.addNoteContent);
        progressBarSave = findViewById(R.id.progressBar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nTitle=noteTitle.getText().toString();
                String nContent=noteContent.getText().toString();
                if(nTitle.isEmpty() || nContent.isEmpty()){
                    Toast.makeText(AddNote.this,"Empty!!! can not saved",Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBarSave.setVisibility(View.VISIBLE);
                // save note

                DocumentReference docref= fStore.collection("note").document(user.getUid()).collection("Notes").document();
                Map<String,Object> note=new HashMap<>();
                note.put("title",nTitle);
                note.put("content",nContent);
                docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddNote.this,"Saved",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                     Toast.makeText(AddNote.this,"Error!!!, Try again",Toast.LENGTH_SHORT).show();
                        progressBarSave.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Toast.makeText(this,"Not saved",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}