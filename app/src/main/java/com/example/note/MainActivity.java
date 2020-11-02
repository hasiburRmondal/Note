package com.example.note;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import authentication.Login;
import authentication.Register;
import model.Adapter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView noteLists;
    Adapter adapter;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Note,NoteViewHolder> noteAdapter;
    FirebaseAuth fAuth;
    FirebaseUser user;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkConnection();

        Toolbar toolbar = findViewById(R.id.noteTitle);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        fAuth=FirebaseAuth.getInstance();
        user=fAuth.getCurrentUser();

        Query query = fStore.collection("note").document(user.getUid()).collection("Notes").orderBy("title", Query.Direction.ASCENDING);


        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class)
                .build();

        noteAdapter=new FirestoreRecyclerAdapter<Note,NoteViewHolder>(allNotes) {
            @SuppressLint("NewApi")
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull final Note note) {
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                final int code = getRandomcolor();
                noteViewHolder.mCardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(code,null));
                final String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), NoteDetails.class);
                        intent.putExtra("title",note.getTitle());
                        intent.putExtra("content",note.getContent());
                        intent.putExtra("code",code);
                        intent.putExtra("noteId",docId);
                        v.getContext().startActivity(intent);
                    }
                });

                ImageView menuIcon=noteViewHolder.view.findViewById(R.id.menuIcon);
                 menuIcon.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         PopupMenu menu = new PopupMenu(v.getContext(),v);
                         menu.setGravity(Gravity.END);
                         menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                             @Override
                             public boolean onMenuItemClick(MenuItem item) {
                                 DocumentReference docref=fStore.collection("note").document(user.getUid()).collection("Notes").document(docId);
                                 docref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                     @Override
                                     public void onSuccess(Void aVoid) {
                                         Toast.makeText(MainActivity.this,"Delete successful",Toast.LENGTH_SHORT).show();
                                     }
                                 }).addOnFailureListener(new OnFailureListener() {
                                     @Override
                                     public void onFailure(@NonNull Exception e) {
                                         Toast.makeText(MainActivity.this,"Error in Deleting",Toast.LENGTH_SHORT).show();
                                     }
                                 });
                                 return false;
                             }
                         });
                         menu.show();
                     }
                 });


            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };

        noteLists = findViewById(R.id.notelist);

        drawerLayout = findViewById(R.id.drawer);
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();


        noteLists.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteLists.setAdapter(noteAdapter);

        View headerView=nav_view.getHeaderView(0);
        TextView username= headerView.findViewById(R.id.textView5);
        TextView userEmail= headerView.findViewById(R.id.textView4);

        if(user.isAnonymous()){
            userEmail.setVisibility(View.INVISIBLE);
            username.setText("Guest Account");
        }
        else {
            userEmail.setText(user.getEmail());
            username.setText(user.getDisplayName());
        }

        FloatingActionButton fab = findViewById(R.id.addNoteFloat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), AddNote.class));
                finish();
            }
        });


    }

    public void checkConnection(){
        ConnectivityManager manager= (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork=manager.getActiveNetworkInfo();

        if(activeNetwork!=null){
            //
        }
        else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {

            case R.id.addnote:
                startActivity(new Intent(this, AddNote.class));
                break;

            case R.id.note:
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.signup:
               if(user.isAnonymous()){
                  sAlert();
               }
               else {
                   Toast.makeText(this, "You are already logged in", Toast.LENGTH_SHORT).show();
               }
                break;

             case R.id.Log:
                 if(user.isAnonymous()){
                     lAlert();
                 }
                 else {
                     Toast.makeText(this, "You are already logged in", Toast.LENGTH_SHORT).show();
                 }
                break;

            case R.id.logout:
                checkUser();
                break;

                default:
                Toast.makeText(this, "Coming soon...", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @SuppressLint("NewApi")
    private void checkUser() {
        if(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isAnonymous()){
            displayAlert();
        }
        else {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(),Splash.class));
            finish();
        }
    }

    private void displayAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("This is guest account,once you logged out all notes will be deleted and if you want to save all notes press Sign Up")
                .setPositiveButton("Sign Up", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        fStore.collection("note").document(user.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this,"Delete Guest Account all notes",Toast.LENGTH_SHORT).show();
                            }
                        });
                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(getApplicationContext(),Splash.class));
                                finish();
                            }
                        });

                    }
                });
        warning.show();
    }

    private void lAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("This is Guest Account,once you log in all note will be deleted")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    }
                }).setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,"Back",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    }
                });
        warning.show();
    }

    private void sAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are you want ?")
                .setMessage("This is Guest Account,if you want to save Guest notes press continue to Sign Up")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,"Back",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    }
                });
        warning.show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.info:
                Toast.makeText(MainActivity.this,"If you want to logout Guest Account all notes will be deleted. If you want to save the Guest Account notes you can Sign Up",Toast.LENGTH_LONG).show();
                break;

            case R.id.exit:
                super.onBackPressed();
                break;

            default:
                Toast.makeText(this, "Coming soon...", Toast.LENGTH_SHORT).show();

        }

        return false;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteContent;
        View view;
        CardView mCardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            mCardView = itemView.findViewById(R.id.noteCard);
            view = itemView;
        }

    }

    private int getRandomcolor(){
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.blue);
        colorCode.add(R.color.yellow);
        colorCode.add(R.color.skyblue);
        colorCode.add(R.color.lightPurple);
        colorCode.add(R.color.lightGreen);
        colorCode.add(R.color.gray);
        colorCode.add(R.color.pink);
        colorCode.add(R.color.greenlight);
        colorCode.add(R.color.notgreen);
        colorCode.add(R.color.colorPrimary);

        Random randomColor = new Random();
        int number = randomColor.nextInt(colorCode.size());
        return colorCode.get(number);

    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(noteAdapter!= null){
            noteAdapter.stopListening();
        }
    }
}
