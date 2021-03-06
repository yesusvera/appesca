package br.org.unesco.appesca.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.org.unesco.appesca.R;
import br.org.unesco.appesca.control.QuestaoDetailFragment;
import br.org.unesco.appesca.dao.FormularioDAO;
import br.org.unesco.appesca.dao.PerguntaDAO;
import br.org.unesco.appesca.dao.QuestaoDAO;
import br.org.unesco.appesca.dao.RespostaDAO;
import br.org.unesco.appesca.model.Formulario;
import br.org.unesco.appesca.model.Identity;
import br.org.unesco.appesca.model.LocResidencia;
import br.org.unesco.appesca.model.Pergunta;
import br.org.unesco.appesca.model.Questao;
import br.org.unesco.appesca.model.Resposta;
import br.org.unesco.appesca.util.ConstantesIdsFormularios;

public class FormCamRegActivityNew extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * ID EXTRAS
     */
    public static String ID_FORMULARIO_OPEN = "ID_FORMULARIO_OPEN";

    public static Formulario formulario;
    public static LocResidencia locResidencia = null;
//    public static List<Questao> questoes = new ArrayList<Questao>();

    public static int  id_activity_questao_atual = 0;
    public static int posAtual = 0;

    private QuestaoDAO questaoDAO;
    private RespostaDAO respostaDAO;
    private PerguntaDAO perguntaDAO;

    View cabecalhoNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formcamreg);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setLogo(R.mipmap.ic_launcher);

        formulario = null;

        questaoDAO = new QuestaoDAO(FormCamRegActivityNew.this);
        respostaDAO = new RespostaDAO(FormCamRegActivityNew.this);
        perguntaDAO = new PerguntaDAO(FormCamRegActivityNew.this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.float_button_menu_save);
        //SALVAR AS RESPOSTAS DA QUESTAO
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    int ordemQuestao = posAtual -1;

                    Questao questao = questaoDAO.findQuestaoByOrdemIdFormulario(ordemQuestao, formulario.getId());


                    //PRIMEIRO EXCLUIR TUDO EM CASCATA: RESPOSTAS, PERGUNTAS E QUESTAO. DEPOIS RE-INSERIR.
                    if(questao!=null) {
                        List<Pergunta> listaPerguntas = questao.getPerguntas();

                        for (Pergunta pergunta : listaPerguntas) {
                            List<Resposta> listaRespostas = pergunta.getRespostas();

                            for (Resposta resposta : listaRespostas) {
                                respostaDAO.delete(resposta.getId());
                            }
                            perguntaDAO.deletePerguntaById(pergunta.getId());
                        }
                        questaoDAO.deleteQuestaoById(questao.getId());
                        questao = null;
                    }

                    if(questao == null){
                        questao = new Questao();
                        questao.setOrdem(ordemQuestao);
                        questao.setIdFormulario(formulario.getId());
                        questao.setTitulo("Questão " + ordemQuestao);
                        questao = questaoDAO.insertQuestao(questao);

                        questao.setPerguntas(buildPerguntaList(questao));

                        if(questao.getPerguntas() != null && !questao.getPerguntas().isEmpty()){
                            questaoDAO.updateQuestao(questao);
                        }
                    }
                Toast.makeText(getApplicationContext(), "Formulário salvo na base local.", Toast.LENGTH_LONG).show();

                if(posAtual == ConstantesIdsFormularios.arrayIdsFormularioCamaraoRegionalApresentacao.length-1){
                    posAtual = -1;
                }

                openFragment(ConstantesIdsFormularios.
                        arrayIdsFormularioCamaraoRegionalApresentacao[++posAtual]);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        drawer.openDrawer(GravityCompat.START);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        cabecalhoNavigationView = navigationView.getHeaderView(0);

        //TODO if(insert){
        inicializaFormulario();
        //}else{//TODO Update{
        //}
        openFragment(R.layout.localizacao_residencia_form);
    }


    public void inicializaFormulario(){
        if(getIntent().getExtras()!=null && getIntent().getExtras().get(ID_FORMULARIO_OPEN)!=null) {
            formulario = (Formulario) getIntent().getExtras().get(ID_FORMULARIO_OPEN);
        }

        Date dtCriacaoFormulario = new Date();

        if(formulario==null) {
            formulario = new Formulario();
            formulario.setIdTipoFormulario(1);
            formulario.setIdUsuario(Identity.getUsuarioLogado().getId());
            formulario.setDataAplicacao(dtCriacaoFormulario);
            formulario.setNome("Formulário Camarão Regional");
            formulario.setSituacao(0);

            FormularioDAO formularioDAO = new FormularioDAO(this);
            formulario = formularioDAO.insertFormulario(formulario);
        }else{
            dtCriacaoFormulario = formulario.getDataAplicacao();
        }

        TextView txtPesquisador = (TextView) cabecalhoNavigationView.findViewById(R.id.txtPesquisador);
        if (Identity.getUsuarioLogado() != null && Identity.getUsuarioLogado().getNome() != null) {
            txtPesquisador.setText(Identity.getUsuarioLogado().getNome());
        }

        TextView txtDataPesquisa = (TextView) cabecalhoNavigationView.findViewById(R.id.txtDataPesquisa);
        txtDataPesquisa.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(dtCriacaoFormulario));

        locResidencia = null;
        id_activity_questao_atual = 0;
        posAtual = 0;
    }


    private List<Pergunta> buildPerguntaList(Questao questao){

        List<Pergunta> perguntas = new ArrayList<Pergunta>();

        /** PERGUNTAS **/
        for(int seqPergunta=1; seqPergunta<=30; seqPergunta++){

            PerguntaDAO perguntaDAO = new PerguntaDAO(this);
            RespostaDAO respostaDAO = new RespostaDAO(this);

            String currentPergunta = ConstantesIdsFormularios.PERGUNTA.concat(String.valueOf(seqPergunta));
//            TextView perguntaTextView = (TextView) findViewById(getResources().getIdentifier(currentPergunta ,"id", getPackageName())); //perg1
//
//            if(perguntaTextView != null) {
                List<Resposta> respostas = new ArrayList<Resposta>();

                Pergunta pergunta = perguntaDAO.findPerguntaByOrdemIdQuestao(seqPergunta, questao.getId());

                if(pergunta == null){
                    pergunta = new Pergunta();
                    pergunta.setOrdem(seqPergunta);
                    pergunta.setBooleana(false);
                    pergunta.setRespBooleana(false);
                    pergunta.setIdQuestao(questao.getId());
                    pergunta = perguntaDAO.insertPergunta(pergunta);
                }

                /** RADIOBUTTON **/
                for (int rb = 1; rb <= 50; rb++) { //
                    String currentRadioButton = currentPergunta.concat(ConstantesIdsFormularios.TYPE_RADIO_BUTTON + rb); //1 ou 2 //3 ou 4 //5 e 6
                    RadioButton radioButton = (RadioButton) findViewById(getResources().getIdentifier(currentRadioButton, "id", getPackageName()));


                    if (radioButton != null && radioButton.isChecked()) {
                        Resposta resp = new Resposta();
                        resp.setOpcao(rb);
                        resp.setIdPergunta(pergunta.getId());
                        resp.setOrdem(rb);

                        respostaDAO.save(resp);

                        respostas.add(resp);
//                        break;
                    }
                }

                /** CHECKBOX **/
                for (int cb = 1; cb <= 50; cb++) {
                    String currentCheckBox = currentPergunta.concat(ConstantesIdsFormularios.TYPE_CHECK_BOX + cb);
                    CheckBox checkBox = (CheckBox) findViewById(getResources().getIdentifier(currentCheckBox, "id", getPackageName()));

                    if (checkBox != null && checkBox.isChecked()) {
                        Resposta resp = new Resposta();
                        resp.setOpcao(cb);
                        resp.setIdPergunta(pergunta.getId());
                        resp.setOrdem(cb);

                        respostaDAO.save(resp);
                        respostas.add(resp);
                    }
                }

                /** EDITTEXT **/
                for (int et = 1; et <= 50; et++) {
                    String currentEditText = currentPergunta.concat(ConstantesIdsFormularios.TYPE_EDIT_TEXT + et);
                    EditText editText = (EditText) findViewById(getResources().getIdentifier(currentEditText, "id", getPackageName()));

                    if (editText != null && !editText.getText().toString().isEmpty()) {
                        Resposta resp = new Resposta();
                        resp.setTexto(editText.getText().toString());
                        resp.setIdPergunta(pergunta.getId());
                        resp.setOrdem(et);

                        respostaDAO.save(resp);
                        respostas.add(resp);
                    }
                }

                pergunta.setRespostas(respostas);
                perguntas.add(pergunta);
            }
//        }
        return perguntas;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_formcamreg, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.itemEnviarFormulario) {

            new AlertDialog.Builder(this)
                    .setTitle("Appesca")
                    .setMessage("Este formulário será enviado para aprovação, confirma o envio?")
                    .setIcon(android.R.drawable.ic_dialog_alert)

                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            formulario.setSituacao(1);
                            FormularioDAO formularioDAO = new FormularioDAO(FormCamRegActivityNew.this);
                            formulario = formularioDAO.insertFormulario(formulario);
                            Toast.makeText(getApplicationContext(), "Formulário salvo na base local.", Toast.LENGTH_LONG).show();Toast.makeText(getApplicationContext(), "Formulário enviado com sucesso!.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
        if (id == R.id.itemClose) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * @author yesus
     * @param idMenuLateral
     * @return
     */
    private int descobrirPos(int idMenuLateral){
        int i=0;
        for(int idMenu: ConstantesIdsFormularios.arraysIdsMenusLaterais){
            if(idMenu == idMenuLateral){
                return i;
            }
            i++;
        }
        return 0;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        posAtual = descobrirPos(id);
        openFragment(ConstantesIdsFormularios.
                arrayIdsFormularioCamaraoRegionalApresentacao[posAtual]);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openFragment(int idLayout) {
        id_activity_questao_atual = idLayout;

        int ordemQuestao = posAtual -1;


        Bundle arguments = new Bundle();
        arguments.putString(QuestaoDetailFragment.ARG_ITEM_ID, String.valueOf(idLayout));
        arguments.putInt(QuestaoDetailFragment.ARG_QUESTAO_ORDEM, ordemQuestao);
        arguments.putInt(QuestaoDetailFragment.ARG_FORMULARIO_ID, formulario.getId());

        QuestaoDetailFragment fragment = new QuestaoDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.questao_detail_container, fragment)
                .commit();
    }
}