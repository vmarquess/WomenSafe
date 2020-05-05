package br.com.victoriasantos.womensafe.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.victoriasantos.womensafe.R
import br.com.victoriasantos.womensafe.domain.LocationData
import br.com.victoriasantos.womensafe.view.activity.ContributionsActivity
import br.com.victoriasantos.womensafe.view.activity.MapsActivity
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.item_evaluation_plates.view.*

class SpotEvaluationAdapter(private val contributionsActivity: ContributionsActivity, private val MapsActivity: MapsActivity, private val dataSet: Array<LocationData>?) : RecyclerView.Adapter<SpotEvaluationAdapter.SpotEvaluationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotEvaluationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evaluation_plates, parent, false)
        return SpotEvaluationViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (dataSet != null) {
            return dataSet.size
        }
        else return 0
    }

    override fun onBindViewHolder(holder: SpotEvaluationViewHolder, position: Int) {
        val LocationData = dataSet?.get(position)
        val endereco = contributionsActivity.getAddress(LatLng(LocationData!!.latitude,LocationData.longitude))
        holder.endereco.text = endereco
        holder.comentario.text = LocationData?.evaluation
        holder.excluir.setOnClickListener{
            contributionsActivity.deleteSpotEvaluation(LocationData.latitude.toString(), LocationData.longitude.toString(), LocationData.evaluation)
        }
    }

    class SpotEvaluationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val endereco: TextView = itemView.texto_id
        val comentario: TextView = itemView.comentario_avl
        val excluir : ImageView = itemView.delete
    }
}