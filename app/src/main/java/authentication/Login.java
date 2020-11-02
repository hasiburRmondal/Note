package authentication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.note.MainActivity;
import com.example.note.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Login extends AppCompatActivity {
    EditText lemail,lpassword;
    Button login;
    TextView forgetpass,createacc;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ProgressBar pbr;
    FirebaseUser user;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        lemail=findViewById(R.id.email);
        lpassword=findViewById(R.id.lPassword);
        login=findViewById(R.id.loginBtn);
        forgetpass=findViewById(R.id.forgotPasword);
        createacc=findViewById(R.id.createAccount);
        pbr= findViewById(R.id.progressBar3);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String memail=lemail.getText().toString();
                String mpass=lpassword.getText().toString();



                if(memail.isEmpty() || mpass.isEmpty()){
                    Toast.makeText(Login.this,"Must be field all",Toast.LENGTH_SHORT).show();
                    return;
                }
                pbr.setVisibility(View.VISIBLE);
                if(Objects.requireNonNull(fAuth.getCurrentUser()).isAnonymous()){
                    FirebaseUser user=fAuth.getCurrentUser();
                    fStore.collection("note").document(user.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                         Toast.makeText(Login.this,"Delete Guest Account all notes",Toast.LENGTH_SHORT).show();
                        }
                    });
                    user.delete();
                }
                fAuth.signInWithEmailAndPassword(memail,mpass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(Login.this,"Login successful",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this,"Login failed"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                        pbr.setVisibility(View.GONE);
                    }
                });
            }
        });

        createacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
                finish();
            }
        });

        forgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetMail= new EditText(v.getContext());
                AlertDialog.Builder password = new AlertDialog.Builder(v.getContext());
                 password.setTitle("Reset Password ?");
                 password.setMessage("Enter your email");
                 password.setView(resetMail);
                 password.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         String mail=resetMail.getText().toString();
                         fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {
                                 Toast.makeText(Login.this,"Reset link sent to your email",Toast.LENGTH_SHORT).show();
                             }
                         }).addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {
                                 Toast.makeText(Login.this,"Error!!"+e.getMessage(),Toast.LENGTH_SHORT).show();
                             }
                         });
                     }
                 });
                 password.setNegativeButton("No", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         startActivity(new Intent(getApplicationContext(), Login.class));
                         finish();
                     }
                 });
                 password.create().show();
            }
        });

    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        Toast.makeText(this,"Incomplete Log In",Toast.LENGTH_SHORT).show();
        finish();

        return super.onOptionsItemSelected(item);
    }
}