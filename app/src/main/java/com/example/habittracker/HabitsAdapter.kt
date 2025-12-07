package com.example.habittracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitsAdapter(
    private val items: MutableList<HabitItem>,
    private val onCheckedChanged: (position: Int, checked: Boolean) -> Unit,
    private val onEditClicked: (position: Int) -> Unit,
    private val onDeleteClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.Holder>() {

    //Holds references to UI elements for one habit item
    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkCompleted: CheckBox = itemView.findViewById(R.id.checkCompleted)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtStreak: TextView = itemView.findViewById(R.id.txtStreak)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)  //Creates view for one habit
        return Holder(v)
    }

    override fun getItemCount(): Int = items.size   //total number of habit items in the list

    //each habit
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.txtName.text = item.name
        holder.txtStreak.text = if (item.streakDays > 0) "Streak: ${item.streakDays} day(s)" else ""
        holder.checkCompleted.setOnCheckedChangeListener(null)
        holder.checkCompleted.isChecked = item.completed
        holder.checkCompleted.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged(holder.bindingAdapterPosition, isChecked)
        }

        holder.btnEdit.setOnClickListener {
            onEditClicked(holder.bindingAdapterPosition)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClicked(holder.bindingAdapterPosition)
        }
    }
    fun getHabitAt(position: Int): HabitItem {
        return items[position]
    }

    fun update(newItems: List<HabitItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}