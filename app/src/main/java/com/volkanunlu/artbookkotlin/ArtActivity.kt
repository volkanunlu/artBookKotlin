package com.volkanunlu.artbookkotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.google.android.material.snackbar.Snackbar
import com.volkanunlu.artbookkotlin.databinding.ActivityArtBinding
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.jar.Manifest

class ArtActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtBinding
    private lateinit var acticityResultLauncher:ActivityResultLauncher<Intent>  //veri alıp dönmesi için
    private lateinit var permissionLauncher: ActivityResultLauncher<String> //İzinler string, izin alabilmek için.
    var selectedBitmap: Bitmap? =null   //Bitmapimi verdim.

    private lateinit var database:SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityArtBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        database=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

        registerLauncher()  //initialize ettik,yoksa kullanamayız.

        val intent=intent
        val info=intent.getStringExtra("info")  //eski mi yoksa yeni bir veri mi bunun kontrolünü yapacağım

        if(info.equals("new")){    //Yeni bir veri ekleneceği zaman artActivity görünümünü dizayn ettik
         binding.artNameText.setText("")
         binding.artistText.setText("")
         binding.yearText.setText("")
         binding.imageView.setImageResource(R.drawable.selectimage)
         binding.save.visibility=View.VISIBLE
        }
        else{ //Eski bir veri geleceği zaman ise buton görünümünü pasif hale getirdik.

            binding.save.visibility=View.INVISIBLE

            //KAYITLI VERİLERİMLE İLGİLİ İŞLEMLERİM.
            val selectedId=intent.getIntExtra("id",1)

                                                                        //? olan alanı arrayof içinde selected id ile bağladık
            val cursor =database.rawQuery("SELECT * FROM arts WHERE id=?", arrayOf(selectedId.toString()))
            val artNameIx=  cursor.getColumnIndex("artname")
            val artistNameIx=cursor.getColumnIndex("artistname")
            val yearIx=cursor.getColumnIndex("year")
            val imageIx=cursor.getColumnIndex("image")

            while (cursor.moveToNext()){

                binding.artNameText.setText(cursor.getString(artNameIx))
                binding.artistText.setText(cursor.getString(artistNameIx))
                binding.yearText.setText(cursor.getString(yearIx))

                val byteArray=cursor.getBlob(imageIx)
                val bitmap=BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)


            }
            cursor.close()

        }





    }

    fun saveButtonClicked(view: View){

            val artName=binding.artNameText.text.toString()
            val artistName=binding.artistText.text.toString()
            val year=binding.yearText.text.toString()


        if(selectedBitmap!=null){

            val smallBitmap=makeSmallerBitmap(selectedBitmap!!,300)

            //Görseli byte verisine dönüştürme
            val outputStream=ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray=outputStream.toByteArray()  //resmimin byte hali

            try {
             // val database=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
             database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY  , artname VARCHAR, artistname VARCHAR , year VARCHAR , image BLOB)" )

             //verileri bağlarken dışardan veri geleceği için statement kullanıyoruz.Önemli Detay Statement koşul stringi ister ve id 1 den başlar.

             val sqlString="INSERT INTO arts(artname,artistname,year,image) VALUES (?,?,?,?)"
             val statement=database.compileStatement(sqlString)
             statement.bindString(1,artName)
             statement.bindString(2,artistName)
             statement.bindString(3,year)
             statement.bindBlob(4,byteArray) //image yazma şaşkınlığı yaşama, veri şeklinde database kayıt yapabilirsin.
             statement.execute() //çalıştırılması için şart


            }

            catch (e:Exception){
                e.printStackTrace()
            }

            val intent=Intent(this@ArtActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //kendinden önceki açık tüm activityleri kapat
            startActivity(intent)



        }



    }

    //SQlite içerisinde kullanacağımız resim row'da 1 mb aşamaz. o yüzden bu fonksiyonu yazıp resimi küçültmeliyiz.
    //Gelecekte ihtiyacın olursa kullanabilirsin. CreateScaleBitmap sınıfı ile büyütüp,küçültebiliyoruz.
    //Bize gelen görselin yatay ve dikey olduğunu bilmemek dezavantaj, bunun algoritmasını biz yazıcaz.
    private fun makeSmallerBitmap(image: Bitmap , maximumSize: Int ) : Bitmap {
        var width=image.width
        var height=image.height

        //Genişlik/yükseklik 1'den küçükse bu dikey , 1'den büyükse bu yatay bir resim demektir.Kare ihtimalinde if'de =1 diye
        //bakabilirsin ama bu örnekte detaylandırmıyorum.
        val bitmapRatio:Double = width.toDouble()/height.toDouble()

        if(bitmapRatio>1){
            //landscape image - yatay

            width=maximumSize
            val scaledHeight= width/bitmapRatio
            height=scaledHeight.toInt()

        }
        else{
            //portrait -- dikey
            height=maximumSize
            val scaledWidth= height*bitmapRatio
            width=scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)

    }



    fun selectImage(view:View){
                                        // galeriye gitmeye izin verilmediyse şartını oluşturdum.
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_DENIED ){

            //rationale --> senden izin istiyorum, şu yüzden.(mantığı gösteriyoruz kullanıcıya)
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                //rationale gösterip izin isteyeceğim alan.

                //Snackbar.LENGTH_INDEFINITE --> kullanıcı tıklayana kadar ekranda snackbar

                Snackbar.make(view,"Permission needed to gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                    View.OnClickListener {  }).show()
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)   //izin alma işlemi

            }
            else{
                //request permission (rationale göstermeden izin isteyeceğim alan)
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)  //izin alma işlemi


            }


        }
        else{  //intent ile galeriye gidicez. İntent action pick ile veri de alabilen bir yapıdır.

            val intentToGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //URI telefonda kayıtlı tutulduğu yer verinin.
            acticityResultLauncher.launch(intentToGallery)


        }

    }


    private fun registerLauncher(){


        //GALERİYE GİDİP VERİ ALMA KISMI
        //StartActivityForResult --> bir sonuç için aktiviteye gidiyor.,
        acticityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode== RESULT_OK){
                val intentFromResult=result.data  //nullable dönmemesi adına yaptık, bir değişkene ve şarta alarak sağlamlaştırdık.
                if(intentFromResult!=null){

                    val imageData=intentFromResult.data  //verinin uri'nı aldım.
                   // binding.imageView.setImageURI(imageData) normalde bu şekilde verebiliriz ama , biz sqlite ile çalışıcaz, bitmap olması lazım.

                    //ImageDecoder ---> Android grafiğin içeriğinde bir sınıf. Uri ' ı görsel yapmaya yarıyor.
                    //Bir adet content resolver (activity) ve bir adet uri ister. Aşağıda kullandık.

                    if(imageData!=null){  // --> imageData boş değilse , ımagedecoder içerisinde altını çiziyor kontrol şart

                    try {  //hataya açık bir durum ve bu durumu ele alıyoruz.

                    if(Build.VERSION.SDK_INT>=28){   //cihazın sdk sürümünü ele alıyoruz, Image Decoder 28 üstü versiyon

                                                    //Bulunduğun aktivite, aldığın image
                    val source=ImageDecoder.createSource(this@ArtActivity.contentResolver,imageData)
                    selectedBitmap=ImageDecoder.decodeBitmap(source)  //bitmape dönüştürdüğüm kısım
                    binding.imageView.setImageBitmap(selectedBitmap)  //binding ile elemente verdim.
                    }
                    else{  //Sdk versiyonu 28 altında olan cihazlardan görsel alma yöntemi

                        selectedBitmap=MediaStore.Images.Media.getBitmap(contentResolver,imageData) //galeriden gittik aldık
                        binding.imageView.setImageBitmap(selectedBitmap)  //binding ile görseli bitmapli halini elemente verdik.

                    }

                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }

                    }

                }

            }
        }
        //  İZİN İSTEME KISMI

        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){ result->
            if(result){
                //permission granted - İzin verildi ise galeriye git , activity ile veriyi al gel.

                val intentToGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                acticityResultLauncher.launch(intentToGallery)



            }
            else{
                //permission denied -- izin verilmedi
                //Toast mesajı gösteriyoruz
                Toast.makeText(this@ArtActivity,"Permission Needed",Toast.LENGTH_LONG).show()

            }

        }

    }


}