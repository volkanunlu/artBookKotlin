package com.volkanunlu.artbookkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.volkanunlu.artbookkotlin.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var artList:ArrayList<Art> //art modelimin tutulacağı arrraylist
    private lateinit var artAdapter:ArtAdapter  //adapterımı globalde tanımladım



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)  //elementlere erişim için klasik bindingimi oluşturdum.
        val view=binding.root
        setContentView(view)

        artList=ArrayList<Art>()  //initilaize ettim

        artAdapter= ArtAdapter(artList) //adapterımı initialize ediyorum
        binding.recyclerView.layoutManager=LinearLayoutManager(this) //veriler nasıl gösterilsin.
        binding.recyclerView.adapter=artAdapter  //boş haldeki yapısına eşitledim,sonrasında veri geldi güncelle ayarını aşağıda vericem.


        try {

            val databace=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

            val cursor=databace.rawQuery("SELECT * FROM arts",null)
            val artNameIX=cursor.getColumnIndex("artname")
            val idIx=cursor.getColumnIndex("id")

            while(cursor.moveToNext()){

                val name=cursor.getString(artNameIX)
                val id=cursor.getInt(idIx)
                val art=Art(name,id)  //modele veri aktarımı
                artList.add(art)

            }

            artAdapter.notifyDataSetChanged()  //veri seti değişti kendini güncelle!
            cursor.close() //imlecimi kapattım

        }
        catch (e:Exception){
            e.printStackTrace()
        }






    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {  //bağlama işlemi yapılacak

        //inflater (menü'nün kendi inflaterı var)

        val menuInflater=menuInflater
        menuInflater.inflate(R.menu.art_menu,menu)   //metottaki değişken ile menuınflaterı bağladık.
        return super.onCreateOptionsMenu(menu)



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //menüye tıklanırsa ne olacak
        if(item.itemId==R.id.add_art_item){  //bir sürü menü seçeneği de olabilirdi , if şartı ile kontrolü o yüzden verdik.

            val intent=Intent(this@MainActivity,ArtActivity::class.java)  //diğer tarafa geçişini sağladık.
            intent.putExtra("info","new") //yeni bir veri eklediğimizi bildirmek adına verdim.
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}