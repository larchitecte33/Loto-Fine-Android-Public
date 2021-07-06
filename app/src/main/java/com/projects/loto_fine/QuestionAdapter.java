package com.projects.loto_fine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.projects.loto_fine.classes_metier.Question;

import java.util.LinkedList;

public class QuestionAdapter extends ArrayAdapter<Question> {
    private final Context context;
    private LinkedList<Question> questions;
    private AppCompatActivity activity;
    private String texteQuestion;
    private int idQuestion;

    public QuestionAdapter(AppCompatActivity activity, Context context, int ressource, LinkedList<Question> questions) {
        super(context, ressource, questions);
        this.activity = activity;
        this.context = context;
        this.questions = questions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_feed_back_adapter, parent, false);
        } else {
            convertView = (LinearLayout) convertView;
        }

        TextView tvQuestion = (TextView)convertView.findViewById(R.id.feed_back_adapter_texte_question);
        RadioButton rbTresBon = (RadioButton)convertView.findViewById(R.id.feed_back_adapter_rb_tres_bon);
        RadioButton rbBon = (RadioButton)convertView.findViewById(R.id.feed_back_adapter_rb_bon);
        RadioButton rbMoyen = (RadioButton)convertView.findViewById(R.id.feed_back_adapter_rb_moyen);
        RadioButton rbTresMoyen = (RadioButton)convertView.findViewById(R.id.feed_back_adapter_rb_tres_moyen);
        RadioButton rbMauvais = (RadioButton)convertView.findViewById(R.id.feed_back_adapter_rb_mauvais);
        RadioGroup rgAppreciation = (RadioGroup)convertView.findViewById(R.id.feed_back_adapter_rg_appreciation);

        System.out.println("rbTresBon.getId() = " + rbTresBon.getId());

        idQuestion = questions.get(position).getId();
        texteQuestion = questions.get(position).getTexte();

        tvQuestion.setText(texteQuestion);

        rgAppreciation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                System.out.println(checkedId);

                if(checkedId == rbTresBon.getId())
                    questions.get(position).setReponseSelectionnee(5);
                else if(checkedId == rbBon.getId())
                    questions.get(position).setReponseSelectionnee(4);
                else if(checkedId == rbMoyen.getId())
                    questions.get(position).setReponseSelectionnee(3);
                else if(checkedId == rbTresMoyen.getId())
                    questions.get(position).setReponseSelectionnee(2);
                else if(checkedId == rbMauvais.getId())
                    questions.get(position).setReponseSelectionnee(1);
            }
        });

        return convertView;
    }
}
