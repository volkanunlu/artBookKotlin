package com.volkanunlu.artbookkotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.volkanunlu.artbookkotlin.databinding.RecyclerRowBinding

class ArtAdapter (val artList : ArrayList<Art>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {


    class ArtHolder (val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root){


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {

        val binding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)   //bindingi oluşturuyoruz.
        return ArtHolder(binding)   //bir artholder geriye döndürüyoruz

    }


    override fun onBindViewHolder(holder: ArtHolder, position: Int) {  //Bağlanınca ne olacak
        holder.binding.recyclerViewTextView.text=artList.get(position).name  //Textini gösterelim, kayıt edildiği ismi
        holder.itemView.setOnClickListener { //tıklandığında ne olacak, yani kayıtlı veriye tıklanınca ne olsun

            val intent=Intent(holder.itemView.context,ArtActivity::class.java)  //intent ile activity geçişi yapıyorum
            intent.putExtra("info","old") //--> kayıtlı bir veri olduğunu bildirmek için
            intent.putExtra("id",artList.get(position).id) //aynı zamanda id'sini de yolluyorum.(new de yapmıcam çünkü ona yeni id gelecek)

            holder.itemView.context.startActivity(intent)

        }


    }

    override fun getItemCount(): Int {
        return artList.size   //recycler view boyutu
    }



}