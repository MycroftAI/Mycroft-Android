/*
 *  Copyright (c) 2017. Mycroft AI, Inc.
 *
 *  This file is part of Mycroft-Android a client for Mycroft Core.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package mycroft.ai.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.user_card_layout.view.*

import mycroft.ai.Utterance
import mycroft.ai.R
import mycroft.ai.UtteranceFrom

/**
 * Created by paul on 2016/06/22.
 */
class MycroftAdapter(private val utteranceList: List<Utterance>) : RecyclerView.Adapter<MycroftAdapter.UtteranceViewHolder>() {

    override fun getItemCount(): Int {
        return utteranceList.size
    }

    override fun onBindViewHolder(utteranceViewHolder: UtteranceViewHolder, i: Int) {
        utteranceViewHolder.vUtterance.text = utteranceList[i].utterance
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): UtteranceViewHolder {
        val itemView = when (i) {
            UtteranceFrom.MYCROFT.id -> LayoutInflater.from(viewGroup.context).inflate(R.layout.mycroft_card_layout, viewGroup, false)
            UtteranceFrom.USER.id -> LayoutInflater.from(viewGroup.context).inflate(R.layout.user_card_layout, viewGroup, false)
            else -> throw IndexOutOfBoundsException("No such view id $i")
        }

        return UtteranceViewHolder(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        val message = utteranceList[position]
        return message.from.id
    }

    class UtteranceViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val vUtterance = v.utterance
    }
}