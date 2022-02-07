package com.example.easysay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.material.navigation.NavigationView;

//начало--------------------------------------------------------------------------------------------------------------------------------
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.Intent;

import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
//--------------------------------------------------------------------------------------------------------------------------------


public class MainActivity extends AppCompatActivity implements CalibrationFragment.OnMessage,  EducationFragment.OnMessageEducation, RecognitionFragment.OnMessageRecognition {

    private TextView textView;
    private TextView textView1;

    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    public Toolbar toolbar;
    public  ActionBarDrawerToggle actionBarDrawerToggle;

    //начало-----------------------------------------------------------------------------------------------------------------------------------------------
    private static final int REQUEST_ENABLE_BT = 1;
    final int ArduinoData = 1;
    final String LOG_TAG = "myLogs";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private static String MacAddress = "98:DA:50:00:38:03"; // MAC-адрес БТ модуля
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ConnectedThred MyThred = null;
    Handler h;
    CountDownTimer timer;

    //--------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //начало----------------------------------------------------------------------------------------------------------------------------------------
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter != null){
            if (btAdapter.isEnabled()){
                Log.d(LOG_TAG, "Bluetooth включен. Все отлично.");
            }else
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                Log.d(LOG_TAG, "Fatal Error: Bluetooth отключен");
            }

        }else
        {
            Log.d(LOG_TAG, "Fatal Error: Bluetooth ОТСУТСТВУЕТ");
        }


        /*b1.setOnClickListener(new View.OnClickListener() {//"Старт распознавания"
            public void onClick(View v) {
                tb1.setText("");
                MyThred.sendByte((byte)1);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {//"Стоп распознавания"
            public void onClick(View v) {
                MyThred.sendByte((byte)0);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {//"Сохранить букву"
            public void onClick(View v) {
                if (tb1.length() != 1) return;
                byte temp = EncodeW1251(tb1.getText().toString())[0];
                tb1.setText("");
                //if (temp < 48) return;
                MyThred.sendByte(temp);
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {//"Очистить память"
            public void onClick(View v) {
                MyThred.sendByte((byte)2);
            }
        });

*/

        timer = new CountDownTimer(10000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                if (режим_распознавания == 1) РаспознаниеПростаивает();
                if (режим_распознавания == 2) ОбучениеПростаивает();
            }
        };

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case ArduinoData:
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = DecodeW1251(readBuf).substring(0, msg.arg1);
                        if (режим_распознавания == 1) РаспознаниеУспешно(strIncom);
                        if (режим_распознавания == 2) ОбучениеУспешно(strIncom);
                        break;
                }
            };
        };



        //-------------------------------------------------------------------------------------------------------------------------------



        // калибровка
        if(findViewById(R.id.fragmentCalibration) != null){
            if(savedInstanceState!=null){
                return;
            }
            CalibrationFragment calibrationFragment = new CalibrationFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().add(R.id.fragmentCalibration, calibrationFragment, null );
            fragmentTransaction.commit();
        }
        // обучение
        if(findViewById(R.id.fragmentEducation) != null){
            if(savedInstanceState!=null){
                return;
            }
            EducationFragment educationFragment = new EducationFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().add(R.id.fragmentEducation, educationFragment, null );
            fragmentTransaction.commit();
        }
        //распознавание
        if(findViewById(R.id.fragmentrecognition) != null){
            if(savedInstanceState!=null){
                return;
            }
            RecognitionFragment recognitionFragment = new RecognitionFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().add(R.id.fragmentrecognition, recognitionFragment, null );
            fragmentTransaction.commit();
        }


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.menu_Open, R.string.menu_Close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                item.setCheckable(true);
                drawerLayout.closeDrawer(GravityCompat.START);
                switch(id){
                    case R.id.nav_connect:
                        ConnectToDevice();
                        item.setTitle("Подключено!!!");
                        item.setEnabled(false);
                        break;
                    case R.id.nav_project:
                        replaceFragment(new ProjectFragment());
                        break;

                    case R.id.nav_calibration:
                        replaceFragment(new CalibrationFragment());
                        break;

                    case R.id.nav_recognition:
                        replaceFragment(new RecognitionFragment());
                        break;

                    case R.id.nav_education :
                        replaceFragment(new EducationFragment());
                        break;


                    case R.id.nav_reference :
                        replaceFragment(new ReferenceFragment());
                        break;
                }
                return true;
            }
        });



    }


    //начало-----------------------------------------------------------------------------------------------------------------------------------------------------
    public static byte[] EncodeW1251(CharSequence string) {
        try {
            ByteBuffer bytes = Charset.forName("windows-1251").newEncoder().encode(CharBuffer.wrap(string));
            byte[] bytesCopy = new byte[bytes.limit()];
            System.arraycopy(bytes.array(), 0, bytesCopy, 0, bytes.limit());
            return bytesCopy;
        }
        catch (CharacterCodingException e) {
            throw new IllegalArgumentException("Encoding failed", e);
        }

    }
    public static String DecodeW1251(byte[] bytearray) {
        try {
            return Charset.forName("windows-1251").newDecoder().decode(ByteBuffer.wrap(bytearray)).toString();
        }
        catch (CharacterCodingException e) {
            throw new IllegalArgumentException("Encoding failed", e);
        }
    }
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------


    private  void replaceFragment(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();


    }
@Override

//обучение
public void OnMessageRead1(String message) {
    ImageView imageView = findViewById(R.id.imageViewPositionHendEdu);
    textView = findViewById(R.id.textView3);
    message.trim();

    textView.setText("Введите букву");

// начать обучение

    ImageView imageView1= findViewById(R.id.imageViewPositionHendEdu);

    imageView1.setImageResource(R.drawable.icons2);

    switch (message) {
        case ("А"):
            imageView.setImageResource(R.drawable.letter1);
            break;
        case ("а"):
            imageView.setImageResource(R.drawable.letter1);
            break;
        case ("Б"):
            imageView.setImageResource(R.drawable.letter2);
            break;
        case ("б"):
            imageView.setImageResource(R.drawable.letter2);
            break;
        case ("В"):
            imageView.setImageResource(R.drawable.letter3);
            break;
        case ("в"):
            imageView.setImageResource(R.drawable.letter3);
            break;
        case ("Г"):
            imageView.setImageResource(R.drawable.letter4);
            break;
        case ("г"):
            imageView.setImageResource(R.drawable.letter4);
            break;
        case ("Д"):
            imageView.setImageResource(R.drawable.letter6);
            break;
        case ("д"):
            imageView.setImageResource(R.drawable.letter6);
            break;
        case ("Е"):
            imageView.setImageResource(R.drawable.letter7);
            break;
        case ("е"):
            imageView.setImageResource(R.drawable.letter7);
            break;
        case ("Ж"):
            imageView.setImageResource(R.drawable.letter8);
            break;
        case ("ж"):
            imageView.setImageResource(R.drawable.letter8);
            break;
        case ("З"):
            imageView.setImageResource(R.drawable.letter9);
            break;
        case ("з"):
            imageView.setImageResource(R.drawable.letter9);
            break;
        case ("И"):
            imageView.setImageResource(R.drawable.letter10);
            break;
        case ("и"):
            imageView.setImageResource(R.drawable.letter10);
            break;
        case ("Й"):
            imageView.setImageResource(R.drawable.letter11);
            break;
        case ("й"):
            imageView.setImageResource(R.drawable.letter11);
            break;
        case ("К"):
            imageView.setImageResource(R.drawable.letter12);
            break;
        case ("к"):
            imageView.setImageResource(R.drawable.letter12);
            break;
        case ("Л"):
            imageView.setImageResource(R.drawable.letter13);
            break;
        case ("л"):
            imageView.setImageResource(R.drawable.letter13);
            break;
        case ("М"):
            imageView.setImageResource(R.drawable.letter14);
            break;
        case ("м"):
            imageView.setImageResource(R.drawable.letter14);
            break;
        case ("н"):
            imageView.setImageResource(R.drawable.letter15);
            break;
        case ("Н"):
            imageView.setImageResource(R.drawable.letter15);
            break;
        case ("О"):
            imageView.setImageResource(R.drawable.letter16);
            break;
        case ("о"):
            imageView.setImageResource(R.drawable.letter16);
            break;
        case ("П"):
            imageView.setImageResource(R.drawable.letter17);
            break;
        case ("п"):
            imageView.setImageResource(R.drawable.letter17);
            break;
        case ("Р"):
            imageView.setImageResource(R.drawable.letter18);
            break;
        case ("р"):
            imageView.setImageResource(R.drawable.letter18);
            break;
        case ("С"):
            imageView.setImageResource(R.drawable.letter19);
            break;
        case ("с"):
            imageView.setImageResource(R.drawable.letter19);
            break;
        case ("Т"):
            imageView.setImageResource(R.drawable.letter20);
            break;
        case ("т"):
            imageView.setImageResource(R.drawable.letter20);
            break;
        case ("У"):
            imageView.setImageResource(R.drawable.letter21);
            break;
        case ("у"):
            imageView.setImageResource(R.drawable.letter21);
            break;
        case ("Ф"):
            imageView.setImageResource(R.drawable.letter22);
            break;
        case ("ф"):
            imageView.setImageResource(R.drawable.letter22);
            break;
        case ("Х"):
            imageView.setImageResource(R.drawable.letter23);
            break;
        case ("х"):
            imageView.setImageResource(R.drawable.letter23);
            break;
        case ("Ц"):
            imageView.setImageResource(R.drawable.letter24);
            break;
        case ("ц"):
            imageView.setImageResource(R.drawable.letter24);
            break;
        case ("Ч"):
            imageView.setImageResource(R.drawable.letter25);
            break;
        case ("ч"):
            imageView.setImageResource(R.drawable.letter25);
            break;
        case ("Ш"):
            imageView.setImageResource(R.drawable.letter26);
            break;
        case ("ш"):
            imageView.setImageResource(R.drawable.letter26);
            break;
        case ("Щ"):
            imageView.setImageResource(R.drawable.letter27);
            break;
        case ("щ"):
            imageView.setImageResource(R.drawable.letter27);
            break;
        case ("Ъ"):
            imageView.setImageResource(R.drawable.letter28);
            break;
        case ("ъ"):
            imageView.setImageResource(R.drawable.letter28);
            break;
        case ("ы"):
            imageView.setImageResource(R.drawable.letter29);
            break;
        case ("Ы"):
            imageView.setImageResource(R.drawable.letter29);
            break;
        case ("Ь"):
            imageView.setImageResource(R.drawable.letter30);
            break;
        case ("ь"):
            imageView.setImageResource(R.drawable.letter30);
            break;
        case ("Э"):
            imageView.setImageResource(R.drawable.letter31);
            break;
        case ("э"):
            imageView.setImageResource(R.drawable.letter31);
            break;
        case ("ю"):
            imageView.setImageResource(R.drawable.letter32);
            break;
        case ("Ю"):
            imageView.setImageResource(R.drawable.letter32);
            break;
        case ("я"):
            imageView.setImageResource(R.drawable.letter33);
            break;
        case ("Я"):
            imageView.setImageResource(R.drawable.letter33);
            break;
        case ("Все откалибровано хорошо"):
            // кнопка откалибровки
            textView1.setText("Все откалибровано хорошо");
            break;
        default:
            textView.setText("Неверно введена буква");
            break;
    }

        }
    // калибровка
    @Override
    public void OnMessageRead(String message) {

        textView = findViewById(R.id.textView3);
        textView1 = findViewById(R.id.textView);
        ImageView imageView = findViewById(R.id.imageViewPositionHendCalibr);
        message.trim();

        textView.setText("Введите букву");
        textView1.setText("");

        switch (message) {
            case ("А"):
                imageView.setImageResource(R.drawable.letter1);
                break;
            case ("а"):
                imageView.setImageResource(R.drawable.letter1);
                break;
            case ("Б"):
                imageView.setImageResource(R.drawable.letter2);
                break;
            case ("б"):
                imageView.setImageResource(R.drawable.letter2);
                break;
            case ("В"):
                imageView.setImageResource(R.drawable.letter3);
                break;
            case ("в"):
                imageView.setImageResource(R.drawable.letter3);
                break;
            case ("Г"):
                imageView.setImageResource(R.drawable.letter4);
                break;
            case ("г"):
                imageView.setImageResource(R.drawable.letter4);
                break;
            case ("Д"):
                imageView.setImageResource(R.drawable.letter6);
                break;
            case ("д"):
                imageView.setImageResource(R.drawable.letter6);
                break;
            case ("Е"):
                imageView.setImageResource(R.drawable.letter7);
                break;
            case ("е"):
                imageView.setImageResource(R.drawable.letter7);
                break;
            case ("Ж"):
                imageView.setImageResource(R.drawable.letter8);
                break;
            case ("ж"):
                imageView.setImageResource(R.drawable.letter8);
                break;
            case ("З"):
                imageView.setImageResource(R.drawable.letter9);
                break;
            case ("з"):
                imageView.setImageResource(R.drawable.letter9);
                break;
            case ("И"):
                imageView.setImageResource(R.drawable.letter10);
                break;
            case ("и"):
                imageView.setImageResource(R.drawable.letter10);
                break;
            case ("Й"):
                imageView.setImageResource(R.drawable.letter11);
                break;
            case ("й"):
                imageView.setImageResource(R.drawable.letter11);
                break;
            case ("К"):
                imageView.setImageResource(R.drawable.letter12);
                break;
            case ("к"):
                imageView.setImageResource(R.drawable.letter12);
                break;
            case ("Л"):
                imageView.setImageResource(R.drawable.letter13);
                break;
            case ("л"):
                imageView.setImageResource(R.drawable.letter13);
                break;
            case ("М"):
                imageView.setImageResource(R.drawable.letter14);
                break;
            case ("м"):
                imageView.setImageResource(R.drawable.letter14);
                break;
            case ("н"):
                imageView.setImageResource(R.drawable.letter15);
                break;
            case ("Н"):
                imageView.setImageResource(R.drawable.letter15);
                break;
            case ("О"):
                imageView.setImageResource(R.drawable.letter16);
                break;
            case ("о"):
                imageView.setImageResource(R.drawable.letter16);
                break;
            case ("П"):
                imageView.setImageResource(R.drawable.letter17);
                break;
            case ("п"):
                imageView.setImageResource(R.drawable.letter17);
                break;
            case ("Р"):
                imageView.setImageResource(R.drawable.letter18);
                break;
            case ("р"):
                imageView.setImageResource(R.drawable.letter18);
                break;
            case ("С"):
                imageView.setImageResource(R.drawable.letter19);
                break;
            case ("с"):
                imageView.setImageResource(R.drawable.letter19);
                break;
            case ("Т"):
                imageView.setImageResource(R.drawable.letter20);
                break;
            case ("т"):
                imageView.setImageResource(R.drawable.letter20);
                break;
            case ("У"):
                imageView.setImageResource(R.drawable.letter21);
                break;
            case ("у"):
                imageView.setImageResource(R.drawable.letter21);
                break;
            case ("Ф"):
                imageView.setImageResource(R.drawable.letter22);
                break;
            case ("ф"):
                imageView.setImageResource(R.drawable.letter22);
                break;
            case ("Х"):
                imageView.setImageResource(R.drawable.letter23);
                break;
            case ("х"):
                imageView.setImageResource(R.drawable.letter23);
                break;
            case ("Ц"):
                imageView.setImageResource(R.drawable.letter24);
                break;
            case ("ц"):
                imageView.setImageResource(R.drawable.letter24);
                break;
            case ("Ч"):
                imageView.setImageResource(R.drawable.letter25);
                break;
            case ("ч"):
                imageView.setImageResource(R.drawable.letter25);
                break;
            case ("Ш"):
                imageView.setImageResource(R.drawable.letter26);
                break;
            case ("ш"):
                imageView.setImageResource(R.drawable.letter26);
                break;
            case ("Щ"):
                imageView.setImageResource(R.drawable.letter27);
                break;
            case ("щ"):
                imageView.setImageResource(R.drawable.letter27);
                break;
            case ("Ъ"):
                imageView.setImageResource(R.drawable.letter28);
                break;
            case ("ъ"):
                imageView.setImageResource(R.drawable.letter28);
                break;
            case ("ы"):
                imageView.setImageResource(R.drawable.letter29);
                break;
            case ("Ы"):
                imageView.setImageResource(R.drawable.letter29);
                break;
            case ("Ь"):
                imageView.setImageResource(R.drawable.letter30);
                break;
            case ("ь"):
                imageView.setImageResource(R.drawable.letter30);
                break;
            case ("Э"):
                imageView.setImageResource(R.drawable.letter31);
                break;
            case ("э"):
                imageView.setImageResource(R.drawable.letter31);
                break;
            case ("ю"):
                imageView.setImageResource(R.drawable.letter32);
                break;
            case ("Ю"):
                imageView.setImageResource(R.drawable.letter32);
                break;
            case ("я"):
                imageView.setImageResource(R.drawable.letter33);
                break;
            case ("Я"):
                imageView.setImageResource(R.drawable.letter33);
                break;
            case ("Все откалибровано хорошо"):
                // кнопка откалибровки
                textView1.setText("Все откалибровано хорошо");
                break;
            default:
                textView.setText("Неверно введена буква");
                break;
        }


    }





    //начало--------------------------------------------------------------------------------------------------------------------------------

    //---------------начало интерфейсов-------------------
    private int режим_распознавания = 0;
    @Override
    public void НачатьРаспознавание() {
        TextView tv_current = findViewById(R.id.tbResultRec);
        tv_current.setText("");
        Button btn = (Button)findViewById(R.id.btnStartRec);
        btn.setEnabled(false);
        btn = (Button)findViewById(R.id.btnStopRec);
        btn.setEnabled(true);
        режим_распознавания = 1;
        MyThred.sendByte((byte)1);
    }
    @Override
    public void ОкончитьРаспознавание() {
        Button btn = (Button)findViewById(R.id.btnStartRec);
        btn.setEnabled(true);
        btn = (Button)findViewById(R.id.btnStopRec);
        btn.setEnabled(false);
        режим_распознавания = 0;
        MyThred.sendByte((byte)0);
    }

    private void РаспознаниеУспешно(String strIncom) {
        TextView tv_current = findViewById(R.id.tbResultRec);
        if (tv_current.getText().equals("-"))tv_current.setText("");
        if (strIncom.equals(".")) tv_current.setText("");
        else tv_current.setText(tv_current.getText() + strIncom);
        timer.start();
    }
    private void РаспознаниеПростаивает() {
        TextView tv_current = findViewById(R.id.tbResultRec);
        tv_current.setText("-");
    }

    @Override
    public void СохранитьПоложениеРуки(String strIncom) {
        if (strIncom.length() != 1) return;
        byte temp = EncodeW1251(strIncom)[0];
        MyThred.sendByte(temp);
    }
    public  void ОчиститьКалибровку()
    {
        MyThred.sendByte((byte)3);
    }

    @Override
    public void НачатьОбучение() {
        ImageView imageView = findViewById(R.id.imageViewResultEdu);
        imageView.setImageResource(R.drawable.icons3);
        Button btn = (Button)findViewById(R.id.btnStartEdu);
        btn.setEnabled(false);
        btn = (Button)findViewById(R.id.btnStopEdu);
        btn.setEnabled(true);
        режим_распознавания = 2;
        MyThred.sendByte((byte)2);
    }
    @Override
    public void ОкончитьОбучение() {
        Button btn = (Button)findViewById(R.id.btnStartEdu);
        btn.setEnabled(true);
        btn = (Button)findViewById(R.id.btnStopEdu);
        btn.setEnabled(false);
        режим_распознавания = 0;
        MyThred.sendByte((byte)0);
    }

    private void ОбучениеУспешно(String strIncom) {
        TextView tv_current = findViewById(R.id.tbInputCharEdu);

        ImageView imageView = findViewById(R.id.imageViewResultEdu);
        imageView.setImageResource(R.drawable.letter1);

        strIncom = strIncom.toUpperCase();
        tv_current.setText(tv_current.getText().toString().toUpperCase());

        if (tv_current.getText().toString().equals(strIncom)) imageView.setImageResource(R.drawable.icons2);
        else imageView.setImageResource(R.drawable.icons3);

        timer.start();
    }

    private void ОбучениеПростаивает() {
        ImageView imageView = findViewById(R.id.imageViewResultEdu);
        imageView.setImageResource(R.drawable.icons3);
    }
    //---------------конец интерфейсов-------------------

    public void ConnectToDevice() {

        BluetoothDevice device = btAdapter.getRemoteDevice(MacAddress);
        Log.d(LOG_TAG, "***Получили удаленный Device***"+device.getName());

        
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d(LOG_TAG, "...Создали сокет...");
        } catch (IOException e) {
            Log.d(LOG_TAG, "Fatal Error: В onResume() Не могу создать сокет: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();
        Log.d(LOG_TAG, "***Отменили поиск других устройств***");

        Log.d(LOG_TAG, "***Соединяемся...***");
        try {
            btSocket.connect();
            Log.d(LOG_TAG, "***Соединение успешно установлено***");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.d(LOG_TAG, "Fatal Error: В onResume() не могу закрыть сокет" + e2.getMessage() + ".");
            }
        }

        MyThred = new ConnectedThred(btSocket);
        MyThred.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "...In onPause()...");

        if (MyThred.status_OutStrem() != null) {
            MyThred.cancel();
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            Log.d(LOG_TAG, "Fatal Error: В onPause() Не могу закрыть сокет" + e2.getMessage() + ".");
        }
    }

    private class ConnectedThred extends Thread{
        private final BluetoothSocket copyBtSocket;
        private final OutputStream OutStrem;
        private final InputStream InStrem;

        public ConnectedThred(BluetoothSocket socket){
            copyBtSocket = socket;
            OutputStream tmpOut = null;
            InputStream tmpIn = null;
            try{
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();
            } catch (IOException e){}

            OutStrem = tmpOut;
            InStrem = tmpIn;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try{
                    bytes = InStrem.read(buffer);
                    h.obtainMessage(ArduinoData, bytes, -1, buffer).sendToTarget();
                }catch(IOException e){break;}

            }

        }

        public void sendData(String message) {
            byte[] msgBuffer = message.getBytes();
            Log.d(LOG_TAG, "***Отправляем данные: " + message + "***"  );

            try {
                OutStrem.write(msgBuffer);
            } catch (IOException e) {}
        }

        public void sendByte(byte msgBuffer) {
            Log.d(LOG_TAG, "***Отправляем данные: " + msgBuffer+ "***"  );
            try {
                OutStrem.write(msgBuffer);
            } catch (IOException e) {}
        }

        public void cancel(){
            try {
                copyBtSocket.close();
            }catch(IOException e){}
        }

        public Object status_OutStrem(){
            if (OutStrem == null){return null;
            }else{return OutStrem;}
        }
    }
    //--------------------------------------------------------------------------------------------------------------------------------
}
