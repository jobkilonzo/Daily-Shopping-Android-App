package com.example.dailyshopping;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dailyshopping.data.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fab;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private RecyclerView recyclerView;

    FirebaseRecyclerAdapter adapter;
    private TextView total_amount;

    private String type;
    private String note;
    private String post_key;
    private int mAmount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List");
        mDatabase.keepSynced(true);
        toolbar = findViewById(R.id.home_toolbar);
        total_amount = findViewById(R.id.total_amount);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.toolbar);

        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recycler_home);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        fetch();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog();

            }
        });
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalAmount = 0;

                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Data data = snapshot.getValue(Data.class);

                    totalAmount += data.getAmount();

                    String amount = String.valueOf(totalAmount);

                    total_amount.setText(String.format("Ksh.%s.00", amount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void customDialog(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(HomeActivity.this);

        View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.input_data, null);

        final AlertDialog dialog = myDialog.create();

        dialog.setView(view);

        final EditText type = view.findViewById(R.id.edit_type);
        final EditText amount = view.findViewById(R.id.edit_amount);
        final EditText note = view.findViewById(R.id.edit_note);
        Button btnSave = view.findViewById(R.id.btnSave);


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mType = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();

                int intAmount = Integer.parseInt(mAmount);

                if (TextUtils.isEmpty(mType)){
                    type.setError("Field required");
                    return;
                }

                if (TextUtils.isEmpty(mAmount)){
                    type.setError("Field required");
                    return;
                }

                if (TextUtils.isEmpty(mNote)){
                    type.setError("Field required");
                    return;
                }

                String id = mDatabase.push().getKey();

                String date = DateFormat.getInstance().format(new Date());

                Data data = new Data(mType, intAmount, mNote, date, id);

                assert id != null;
                mDatabase.child(id).setValue(data);

                Toast.makeText(getApplicationContext(), "Data saved", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });


        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void fetch(){
        Query query = mDatabase;

        FirebaseRecyclerOptions<Data> options =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(query, new SnapshotParser<Data>() {
                            @NonNull
                            @Override
                            public Data parseSnapshot(@NonNull DataSnapshot snapshot) {
                                String am = String.valueOf(snapshot.child("amount").getValue());
                                int amount = Integer.parseInt(am);
                                return new Data(snapshot.child("type").getValue().toString(),
                                        amount,
                                        snapshot.child("note").getValue().toString(),
                                        snapshot.child("date").getValue().toString(),
                                        snapshot.child("id").getValue().toString());
                            }
                        })
                        .build();
        adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, final int position, @NonNull final Data model) {
                holder.setType(model.getType());

                final int amount = model.getAmount();
                String sAmount = String.valueOf(amount);
                holder.setAmount(sAmount);
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        post_key = getRef(position).getKey();
                        type = model.getType();
                        note = model.getNote();
                        mAmount = model.getAmount();


                        updateData();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_data, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mType;
        TextView mNote;
        TextView mDate;
        TextView mAmount;


        public MyViewHolder(@NonNull View view) {
            super(view);
            mType = view.findViewById(R.id.type);
            mNote = view.findViewById(R.id.note);
            mDate= view.findViewById(R.id.date);
            mAmount = view.findViewById(R.id.amount);

        }
        public void setType(String type){

            mType.setText(type);
        }
        public void setNote(String note){

            mNote.setText(note);
        }
        public void setDate(String date){
            mDate.setText(date);
        }
        public void setAmount(String amount){

            String stam = String.valueOf(amount);
            mAmount.setText(String.format("Ksh.%s.00", stam));
        }
    }

    public void updateData(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(HomeActivity.this);
        View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.update_data, null);

        final AlertDialog alertDialog = myDialog.create();
        alertDialog.setView(view);

        final EditText ed_type = view.findViewById(R.id.edit_type_update);
        final EditText ed_amount = view.findViewById(R.id.edit_amount_update);
        final EditText ed_note = view.findViewById(R.id.edit_note_update);

        ed_type.setText(type);
        ed_type.setSelection(type.length());
        ed_amount.setText(String.valueOf(mAmount));
        ed_amount.setSelection(String.valueOf(mAmount).length());
        ed_note.setText(note);
        ed_note.setSelection(note.length());

        Button btnDelet = view.findViewById(R.id.btnDelete);
        Button btnUpdate = view.findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type = ed_type.getText().toString().trim();
                note = ed_note.getText().toString().trim();
                String amount = ed_amount.getText().toString().trim();

                int intAmount = Integer.parseInt(amount);

                String date = DateFormat.getInstance().format(new Date());

                Data data = new Data(type, intAmount, note, date, post_key);
                mDatabase.child(post_key).setValue(data);

                alertDialog.dismiss();
            }
        });

        btnDelet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(post_key).removeValue();
                alertDialog.dismiss();

            }
        });
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
