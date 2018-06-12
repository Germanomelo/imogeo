/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.ifpb.tccii.imogeo.controller.managedbean;

import br.com.ifpb.tccii.imogeo.model.Comentario;
import br.com.ifpb.tccii.imogeo.model.Imagem;
import br.com.ifpb.tccii.imogeo.model.Imovel;
import br.com.ifpb.tccii.imogeo.model.Usuario;
import br.com.ifpb.tccii.imogeo.model.especializacao.Apartamento;
import br.com.ifpb.tccii.imogeo.model.especializacao.Casa;
import br.com.ifpb.tccii.imogeo.controller.dao.ApartamentoDAO;
import br.com.ifpb.tccii.imogeo.controller.dao.CasaDAO;
import br.com.ifpb.tccii.imogeo.controller.dao.ComentarioDAO;
import br.com.ifpb.tccii.imogeo.controller.dao.ImagemDAO;
import br.com.ifpb.tccii.imogeo.controller.dao.ImovelDAO;
import br.com.ifpb.tccii.imogeo.controller.dao.UsuarioDAO;
import br.com.ifpb.tccii.imogeo.controller.utils.ImagemManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author germano
 */
@ManagedBean(name = "imoveisFavoritosMB")
@ViewScoped
public class ImoveisFavoritosMB implements Serializable {

    private boolean exibeFavoritos = true;
    private boolean exibeDetalhesCasa = false;
    private boolean exibeDetalhesApto = false;
    private String titulo = "Favoritos";
    private String comentarioTxt;
    private double lat;
    private double log;
    
    @EJB
    UsuarioDAO userDao;
    @EJB
    ImovelDAO imovelDao;
    @EJB
    ApartamentoDAO aptoDao;        
    @EJB
    CasaDAO casaDao;
    @EJB
    ComentarioDAO comentarioDao;
    @EJB
    ImagemDAO imagemDao;

    private Usuario userSession = new Usuario();
    private List<Imovel> favoritos;
    private Casa casa = new Casa();
    private Apartamento apto = new Apartamento();
    private Imovel imovel = new Imovel();
    private Comentario comentario = new Comentario();

    public ImoveisFavoritosMB() {
        this.capturarUserSession();
    }

    public List<Imovel> buscaFavoritos() {
        this.favoritos = imovelDao.listarImoveisFavoritos(userSession);
        for (Imovel i : favoritos) {
            Imagem img = imagemDao.retornarPrimeriaImagemPorImovel(i);
            if (img == null) {
                img = new Imagem();
                img.setNome("/img/semimagem.png");
            }else{
                ImagemManager im = new ImagemManager();
                im.criarImagens(null, img);
                img.setNome("/temp/"+img.getNome());
            }
            i.setImagem(img);
        }
        return this.favoritos;
    }

    public void capturarUserSession() {
        HttpSession session;
        session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        this.userSession = (Usuario) session.getAttribute("usuario");
    }
    
    public void detalhesImovel() {
        if (this.imovel instanceof Casa) {
            this.detalhesCasa();
        } else if (this.imovel instanceof Apartamento) {
            this.detalhesApto();
        } else {
            this.mensagemErro("Erro!", "Erro ao ver informações de Imovél, tente mais tarde");
        }
//        this.capturarUserSession();
    }
    
     public void detalhesCasa() {
        try {
            this.casa = casaDao.retornarCasa(this.imovel.getId());
        } catch (Exception e) {
            this.mensagemErro("Erro!", e.getMessage());
        }
        this.telaExibeDetalhesCasa();
    }

    public void detalhesApto() {
        try {
            this.apto = aptoDao.retornarApartamento(this.imovel.getId());
        } catch (Exception e) {
            this.mensagemErro("Erro!", e.getMessage());
        }
        this.telaExibeDetalhesApto();
    }
    
    public void removerImovelFavoritos(){
        if(this.exibeDetalhesCasa){
            for(Imovel n: this.favoritos){
                if(n.getId() == this.casa.getId())
                this.favoritos.remove(n); 
            }
        }else if(this.exibeDetalhesApto){
            for(Imovel n: this.favoritos){
                if(n.getId() == this.apto.getId())
                this.favoritos.remove(n); 
            }
        }
        this.userSession.setFavoritos(this.favoritos);
        userDao.atualizarUsuario(userSession);
        this.telaExibeFavoritos();
    }
    
    public void inserirComentario() {
        Comentario c = new Comentario();
        c.setComentario(this.comentarioTxt);
        c.setDataComentario(new Date());
        c.setUsuario(userSession);
        if (exibeDetalhesCasa) {
            c.setImovel(this.casa);
        } else if (exibeDetalhesApto) {
            c.setImovel(this.apto);
        }
        this.comentarioDao.inserirComentario(c);
        this.comentarioTxt = "";
    }

    public List<Comentario> listarComentarios() {
        List<Comentario> comentarios = new ArrayList<Comentario>();
        if (exibeDetalhesCasa) {
            comentarios = comentarioDao.listarComentariosIdImovel(this.casa);
        } else if (exibeDetalhesApto) {
            comentarios = comentarioDao.listarComentariosIdImovel(this.apto);
        }
        return comentarios;
    }

    public void removerComentario() {
        comentarioDao.removerComentario(this.comentario);
    }

// telas de exibição
    public void telaExibeFavoritos(){     
        this.exibeFavoritos = true;
        this.exibeDetalhesApto = false;
        this.exibeDetalhesCasa = false;
        this.titulo = "Favoritos";
    }
    
    public void telaExibeDetalhesCasa(){     
        this.exibeFavoritos = false;
        this.exibeDetalhesApto = false;
        this.exibeDetalhesCasa = true;
        this.titulo = "Favorito";
        this.lat = this.casa.getEndereco().getLocalizacao().getCoordinate().x;
        this.log = this.casa.getEndereco().getLocalizacao().getCoordinate().y;
    }

    public void telaExibeDetalhesApto(){     
        this.exibeFavoritos = false;
        this.exibeDetalhesApto = true;
        this.exibeDetalhesCasa = false;
        this.titulo = "Favorito";
        this.lat = this.casa.getEndereco().getLocalizacao().getCoordinate().x;
        this.log = this.casa.getEndereco().getLocalizacao().getCoordinate().y;
    }
//      Gets e Sets
    public Usuario getUserSession() {
        return userSession;
    }

    public void setUserSession(Usuario userSession) {
        this.userSession = userSession;
    }

    public boolean isExibeFavoritos() {
        return exibeFavoritos;
    }

    public void setExibeFavoritos(boolean exibeFavoritos) {
        this.exibeFavoritos = exibeFavoritos;
    }

    public boolean isExibeDetalhesCasa() {
        return exibeDetalhesCasa;
    }

    public void setExibeDetalhesCasa(boolean exibeDetalhesCasa) {
        this.exibeDetalhesCasa = exibeDetalhesCasa;
    }

    public boolean isExibeDetalhesApto() {
        return exibeDetalhesApto;
    }

    public void setExibeDetalhesApto(boolean exibeDetalhesApto) {
        this.exibeDetalhesApto = exibeDetalhesApto;
    }

    public List<Imovel> getFavoritos() {
        return favoritos;
    }

    public void setFavoritos(List<Imovel> favoritos) {
        this.favoritos = favoritos;
    }

    public Casa getCasa() {
        return casa;
    }

    public void setCasa(Casa casa) {
        this.casa = casa;
    }

    public Apartamento getApto() {
        return apto;
    }

    public void setApto(Apartamento apto) {
        this.apto = apto;
    }

    public Imovel getImovel() {
        return imovel;
    }

    public void setImovel(Imovel imovel) {
        this.imovel = imovel;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLog() {
        return log;
    }

    public void setLog(double log) {
        this.log = log;
    }

    public String getComentarioTxt() {
        return comentarioTxt;
    }

    public void setComentarioTxt(String comentarioTxt) {
        this.comentarioTxt = comentarioTxt;
    }

    public Comentario getComentario() {
        return comentario;
    }

    public void setComentario(Comentario comentario) {
        this.comentario = comentario;
    }
    
    public List<Imagem> listImagens() {
        ImagemManager imagemManager = new ImagemManager();
        List<Imagem> imagens = imagemDao.listarImagensPorImovel(imovel);
        imagemManager.criarImagens(imagens, null);
        return imagens;
    }
    
    //Mensagens------------------------------>>>>>>>>>>>>>>>
    public void mensagemInformativa(String titulo, String msg) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage fm = new FacesMessage(titulo, msg);
        fm.setSeverity(FacesMessage.SEVERITY_INFO);
        fc.addMessage(null, fm);
    }

    public void mensagemErro(String titulo, String msg) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage fm = new FacesMessage(titulo, msg);
        fm.setSeverity(FacesMessage.SEVERITY_ERROR);
        fc.addMessage(null, fm);
    }

    public void mensagemAlerta(String titulo, String msg) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage fm = new FacesMessage(titulo, msg);
        fm.setSeverity(FacesMessage.SEVERITY_WARN);
        fc.addMessage(null, fm);
    }

}