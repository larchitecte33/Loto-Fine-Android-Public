package com.projects.loto_fine.adapters;

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

import com.projects.loto_fine.R;
import com.projects.loto_fine.classes_metier.Question;

import java.util.LinkedList;

/**
 * ArrayAdapter permettant l'affichage des éléments de la liste des questions.
 */
public class QuestionAdapter extends ArrayAdapter<Question> {
    private final Context context;
    private LinkedList<Question> questions; // Liste des questions.
    private AppCompatActivity activity;
    private String texteQuestion;
    private int idQuestion;

    /**
     * Constructeur
     * @param activity : activité appelante.
     * @param context : contexte de l'activité appelante.
     * @param ressource : ID du layout affichant un élément de la liste des questions.
     * @param questions : liste des questions.
     */
    public QuestionAdapter(AppCompatActivity activity, Context context, int ressource, LinkedList<Question> questions) {
        super(context, ressource, questions);
        this.activity = activity;
        this.context = context;
        this.questions = questions;
    }

    /**
     * Fonction utilisée pour convertir un objet de la liste des questions en vue.
     * @param position : la position de l'item dans l'ensemble de données de l'adapter.
     * @param convertView : l'ancienne vue à réutiliser (si c'est possible, pour des questions de performances).
     * @param parent : vue contenant cette vue.
     * @return la vue qui affiche les données à la position d'index position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // S'il n'existe pas de vue qu'on peut réutiliser pour afficher la vue en cours d'affichage, on va la construire.
        if (convertView == null) {
            // On récupère une instance de LayoutInflater standard qui est déjà connectée au contexte actuel et correctement configurée pour l'appareil.
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // On construit un objet View à partir du fichier XML R.layout.activity_feed_back_adapter.
            convertView = inflater.inflate(R.layout.activity_feed_back_adapter, parent, false);
        } else {
            // Sinon, on va la réutiliser.
            convertView = (LinearLayout) convertView;
        }

        // Récupération des composants.
        TextView tvQuestion = (TextView)convertView.findViewById(R.id.feed_back_adapter_texte_question);
        RadioButton rbTresBon = (RadioButton)convertView.findViewById(R.id.feed_back_adapter_rb_tres_bon);
        RadioButton rbBon = (RadioButton)convertView.findViewById(R.id.feed_back_adapter_rb_bon);
        RadioButton rbMoyen = (RadioButton)convertView.findViewById(R.id.feed_back_adapter_rb_moyen);
        RadioButton rbTresMoyen = (RadioButton)convertView.findViewById(R.id.feed_back_adapter_rb_tres_moyen);
        RadioButton rbMauvais = (RadioButton)convertView.findViewById(R.id.feed_back_adapter_rb_mauvais);
        RadioGroup rgAppreciation = (RadioGroup)convertView.findViewById(R.id.feed_back_adapter_rg_appreciation);

        System.out.println("rbTresBon.getId() = " + rbTresBon.getId());

        // Récupération des valeurs de l'item affiché.
        idQuestion = questions.get(position).getId();
        texteQuestion = questions.get(position).getTexte();

        // On affecte aux composants les valeurs de l'item affiché.
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
