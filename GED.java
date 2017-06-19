package br.com.oma.omaonline.managedbeans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.joda.time.DateTime;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.com.oma.intranet.dao.ControleContasNovoDAO;
import br.com.oma.intranet.dao.LancamentoDAO;
import br.com.oma.intranet.entidades.intra_condominios;
import br.com.oma.intranet.entidades.intra_controle_contas2;
import br.com.oma.intranet.entidades.intra_controle_contas_detalhamento;
import br.com.oma.intranet.entidades.intra_grupo_gerente;
import br.com.oma.intranet.entidades.intra_grupo_permissao;
import br.com.oma.intranet.managedbeans.SessaoMB;
import br.com.oma.intranet.util.CodigoBarras;
import br.com.oma.intranet.util.EnvioEmail;
import br.com.oma.intranet.util.IntranetException;
import br.com.oma.intranet.util.Mensagens;
import br.com.oma.intranet.util.PDFUtil;
import br.com.oma.intranet.util.RNException;
import br.com.oma.intranet.util.ValidaCPFCNPJ;
import br.com.oma.omaonline.dao.BlackListDAO;
import br.com.oma.omaonline.dao.FinanceiroDAO;
import br.com.oma.omaonline.dao.FinanceiroImpostosDAO;
import br.com.oma.omaonline.dao.FinanceiroSIPDAO;
import br.com.oma.omaonline.entidades.atbancos;
import br.com.oma.omaonline.entidades.black_list;
import br.com.oma.omaonline.entidades.cndcondo_param;
import br.com.oma.omaonline.entidades.cndpagar;
import br.com.oma.omaonline.entidades.cndpagar_aprovacao;
import br.com.oma.omaonline.entidades.cndpagar_followup;
import br.com.oma.omaonline.entidades.cndplano;
import br.com.oma.omaonline.entidades.consulta_financeiro;
import br.com.oma.omaonline.entidades.cpcredor;
import br.com.oma.omaonline.entidades.cpfavor;
import br.com.oma.omaonline.entidades.financeiro_imagem;
import br.com.oma.omaonline.entidades.rateio;
import br.com.oma.omaonline.util.ReturnUltimoControlRatRN;
import br.com.oma.sigadm.entidades.sgimpos;

@ManagedBean(name = "ged")
@ViewScoped
public class GED extends Mensagens {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4884066169786728271L;

	// DEPENDENCIA
	@ManagedProperty(value = "#{SessaoMB}")
	private SessaoMB sessaoMB;

	// OBJETOS
	private FinanceiroDAO fncDAO;
	private CodigoBarras codigoBarrasUtil;
	private ValidaCPFCNPJ validaCpfCnpj;
	private consulta_financeiro pagar = new consulta_financeiro();
	private cndpagar cndpagar = new cndpagar();
	private cndpagar pagarLS = new cndpagar();
	private cndplano cndplanoNome = new cndplano();
	private cndcondo_param condoParam = new cndcondo_param();
	private intra_grupo_gerente gerenteMB = new intra_grupo_gerente();
	private intra_condominios icBean = new intra_condominios();
	private rateio rat = new rateio();
	private cndplano cndplano = new cndplano();
	private cpcredor fornecedorSelecionado;
	private intra_grupo_gerente gerenteBEAN = new intra_grupo_gerente();
	private atbancos bancoSelecionado;
	private cpfavor favorecidoSelecionado;
	private intra_controle_contas_detalhamento iccd = new intra_controle_contas_detalhamento();

	// Atributos / Listas
	private List<consulta_financeiro> listarGed, filtroGed, listarGedAprovados, filtroGedAprovados;
	private List<cndpagar> listarCndpagarContas, listarCndpagarGerente, listaDeDuplicidade, listaDePagar, filtroDePagar,
			lstLancamentos, lstRateioAlteracao, listaAprovadosPagar, filtroAprovadosPagar, filtroCndpagarOma,
			filtroCndpagarGerente, filtroCndpagarContas, selectCndpagar, listaAlteracaoContas;
	private List<String> listaDeCodReduzido;
	private List<cndpagar_followup> lstFollowUpTbl, fltrFollowUp, lstFollowUp;
	private List<intra_grupo_gerente> listaDeGerentes;
	private List<intra_condominios> listaDeCondominio;
	private List<financeiro_imagem> listaArquivos = new ArrayList<financeiro_imagem>();
	private List<cndplano> lstConta;
	private List<cpcredor> lstFornecedor;
	private List<cpfavor> lstFavorecido;
	private List<atbancos> lstBancos;
	private List<rateio> listaDeRateio;
	private List<Integer> lstExcArquivo, lstNroLancto;
	private List<sgimpos> listaDeTributos;

	// Atributos
	private int checkCPF;

	private Date filtroCndpagarContasInicial = new Date();
	private Date filtroCndpagarContasFinal = new Date();

	private Date filtroCndpagarGerInicial = new Date();
	private Date filtroCndpagarGerFinal = new Date();

	private int condominio;
	private int gerente;

	private String nomeCondominio;
	private String nomeCondo;
	private double valorBruto;
	private String nomeArquivo;
	private byte[] arquivo;
	private StreamedContent arquivoDownload;

	private boolean proxima1 = true;
	private boolean proxima2;

	// Exibe PDF
	private int cdFinancImagem;
	// Consulta Followup
	private Date dtInicioFollowup;
	private Date dtFimFollowup;
	private String[] acoes;
	private String nomeDoPlanoContas;

	// Infos de tipo de conta
	private String codBanco;
	private String nomeDoBanco;
	private String contaPoupanca;
	private String codAgencia;
	private String cc;
	private String dac;
	private String tipoPessoa;
	private String cpf_cnpj;
	private String tipoPagto;
	private String codMovimento;
	private String codCompensacao;
	private String codigoBarras;
	private String ldCampo1;
	private String ldCampo2;
	private String ldCampo3;
	private String ldDac;
	private String ldValor;
	private String concSegbarra1;
	private String concSegbarra2;
	private String concSegbarra3;
	private String concSegbarra4;
	private String codBarras;
	private boolean usarTED;

	// Pesquisa favorecido
	private String nomeFavorecido;
	private int codigoFavorecido;

	// OBS Lançamento
	private String obsLancto;

	private boolean flagObs;

	// Validação de Lançamentos
	private int valCodLacto;
	private Date valDatVenc;
	private String valCodBarras;
	private String valCodAg;
	private String valCC;
	private double valValor;
	private boolean valValida;
	private boolean validaEtiqueta;

	// Consulta Lancamentos
	private String listarPor;
	private Date dataInicio;
	private Date dataFim;
	private boolean gridTblLancamentos = true;
	private boolean gridDetalhesLancamento = false;
	private financeiro_imagem imagemSelecionada;
	private String nomeImagem;
	private boolean alteracao = false;

	// Grid Wizard Lançamento
	private boolean grid1 = true;
	private boolean grid2 = false;
	private boolean grid3 = false;
	private boolean grid4 = false;

	// Infos de lançamento
	private String idLancamento;
	private Date vencimento;
	private String valor;
	private String contaContabil;
	private String fornecedor;
	private String complemento;
	private String complemento1;
	private String complemento2;
	private String complemento3;
	private String complemento4;
	private String complemento5;
	private String complemento6;
	private String notaFiscal;
	private Date dtEmissaoNF;
	private String empresa;
	private String tipoPagamento;
	private String favorecido;
	private String classificacao;
	private String tipoDocumento = "N.F";
	private String tipoDocumentoPesquisa;
	private int validaAlterar;

	// info rateio
	private String rateado = "N";
	private String valor1;
	private String valor2;
	private String valor3;
	private String valor4;
	private String valor5;
	private String valor6;

	private int qtdeDeContas;

	private String codRed1;
	private String codRed2;
	private String codRed3;
	private String codRed4;
	private String codRed5;
	private String codRed6;

	// pesquisa linha digitavel
	private String linhaDigitavel;

	// Pesquisa conta
	private int codigoConta;
	private String nomeConta;
	private String nomeCapa;
	private String idImagem;

	private int cdImagem;

	// Pesquisa Fornecedor
	private String nomeFornecedor;

	private String historicoExibicaoRateado;

	// Parcelado
	private String parcelamento = "N";
	private String pci;
	private String pcf;

	private String codigoHistPadrao;
	private String msgHistPadrao;
	private double valorGed;
	private int hideSalvar;

	private int opcaoFiltro;
	private int opcaoLancto;
	private int opcaoGerente;

	private String bloco;

	private black_list blackListMB = new black_list();

	private financeiro_imagem imagemDesfazer;
	private int paginaCancelamento;
	private String ordemFusao = "ANTES";
	private int pagina = 1;

	// GET x SET

	public int getOpcaoFiltro() {
		return opcaoFiltro;
	}

	public List<cndpagar> getFiltroCndpagarGerente() {
		return filtroCndpagarGerente;
	}

	public void setFiltroCndpagarGerente(List<cndpagar> filtroCndpagarGerente) {
		this.filtroCndpagarGerente = filtroCndpagarGerente;
	}

	public List<cndpagar> getFiltroCndpagarContas() {
		return filtroCndpagarContas;
	}

	public void setFiltroCndpagarContas(List<cndpagar> filtroCndpagarContas) {
		this.filtroCndpagarContas = filtroCndpagarContas;
	}

	public Date getFiltroCndpagarGerInicial() {
		return filtroCndpagarGerInicial;
	}

	public void setFiltroCndpagarGerInicial(Date filtroCndpagarGerInicial) {
		this.filtroCndpagarGerInicial = filtroCndpagarGerInicial;
	}

	public Date getFiltroCndpagarGerFinal() {
		return filtroCndpagarGerFinal;
	}

	public void setFiltroCndpagarGerFinal(Date filtroCndpagarGerFinal) {
		this.filtroCndpagarGerFinal = filtroCndpagarGerFinal;
	}

	public Date getFiltroCndpagarContasInicial() {
		return filtroCndpagarContasInicial;
	}

	public void setFiltroCndpagarContasInicial(Date filtroCndpagarContasInicial) {
		this.filtroCndpagarContasInicial = filtroCndpagarContasInicial;
	}

	public Date getFiltroCndpagarContasFinal() {
		return filtroCndpagarContasFinal;
	}

	public void setFiltroCndpagarContasFinal(Date filtroCndpagarContasFinal) {
		this.filtroCndpagarContasFinal = filtroCndpagarContasFinal;
	}

	public int getCheckCPF() {
		if (this.cndpagar != null) {
			if (this.cndpagar.getCnpj() > 0 & this.checkCPF == 0) {
				this.validaCPFIcon();
			}
		}
		return checkCPF;
	}

	public void setCheckCPF(int checkCPF) {
		this.checkCPF = checkCPF;
	}

	public String getBloco() {
		return bloco;
	}

	public void setBloco(String bloco) {
		this.bloco = bloco;
	}

	public financeiro_imagem getImagemDesfazer() {
		return imagemDesfazer;
	}

	public void setImagemDesfazer(financeiro_imagem imagemDesfazer) {
		this.imagemDesfazer = imagemDesfazer;
	}

	public int getPaginaCancelamento() {
		return paginaCancelamento;
	}

	public void setPaginaCancelamento(int paginaCancelamento) {
		this.paginaCancelamento = paginaCancelamento;
	}

	public String getOrdemFusao() {
		return ordemFusao;
	}

	public void setOrdemFusao(String ordemFusao) {
		this.ordemFusao = ordemFusao;
	}

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	public int getCodigoFavorecido() {
		return codigoFavorecido;
	}

	public void setCodigoFavorecido(int codigoFavorecido) {
		this.codigoFavorecido = codigoFavorecido;
	}

	public int getOpcaoLancto() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		Map<String, String> params = ctx.getExternalContext().getRequestParameterMap();

		String opcao = params.get("opcaoLancto");
		if (opcao != null) {
			this.opcaoLancto = Integer.valueOf(opcao);
		}
		return opcaoLancto;
	}

	public void setOpcaoLancto(int opcaoLancto) {
		this.opcaoLancto = opcaoLancto;
	}

	public int getOpcaoGerente() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		Map<String, String> params = ctx.getExternalContext().getRequestParameterMap();

		String opcao = params.get("opcaoGerente");
		if (opcao != null) {
			this.opcaoGerente = Integer.valueOf(opcao);
		}
		return opcaoGerente;
	}

	public void setOpcaoGerente(int opcaoGerente) {
		this.opcaoGerente = opcaoGerente;
	}

	public cndpagar getPagarLS() {
		return pagarLS;
	}

	public void setPagarLS(cndpagar pagarLS) {
		this.pagarLS = pagarLS;
	}

	public void setOpcaoFiltro(int opcaoFiltro) {
		this.opcaoFiltro = opcaoFiltro;
	}

	public void setSessaoMB(SessaoMB sessaoMB) {
		this.sessaoMB = sessaoMB;
	}

	public consulta_financeiro getPagar() {
		return pagar;
	}

	public void setPagar(consulta_financeiro pagar) {
		this.pagar = pagar;
	}

	public List<consulta_financeiro> getListarGed() {
		if (!this.sessaoMB.getUsuario().getGrupoGer().isEmpty()) {
			if (this.sessaoMB.getUsuario().getGrupoGer().get(0).getNome().equals(" Todos")) {
				this.listarGed = null;
				this.filtroDePagar = null;
				if (this.listarGed == null) {
					this.fncDAO = new FinanceiroDAO();
					this.listarGed = this.fncDAO.listarGED();
				}
				return this.listarGed;
			} else {
				this.listarGed = null;
				this.filtroDePagar = null;
				if (!this.sessaoMB.getUsuario().getGrupoGer().isEmpty()) {
					if (this.listarGed == null) {
						this.fncDAO = new FinanceiroDAO();
						this.listarGed = this.fncDAO.listarGED(this.gerente);
					}
					return this.listarGed;
				} else {
					return null;
				}
			}
		} else {
			return null;
		}
	}

	public void setListarGed(List<consulta_financeiro> listarGed) {
		this.listarGed = listarGed;
	}

	public List<consulta_financeiro> getListarGedAprovados() {
		if (!this.sessaoMB.getUsuario().getGrupoGer().isEmpty()) {
			if (this.sessaoMB.getUsuario().getGrupoGer().get(0).getNome().equals(" Todos")) {
				this.listarGedAprovados = null;
				this.filtroDePagar = null;
				if (this.listarGedAprovados == null) {
					this.fncDAO = new FinanceiroDAO();
					this.listarGedAprovados = this.fncDAO.listarGEDAprovados();
				}
				return this.listarGedAprovados;
			} else {
				this.listarGedAprovados = null;
				this.filtroDePagar = null;
				if (!this.sessaoMB.getUsuario().getGrupoGer().isEmpty()) {
					if (this.listarGedAprovados == null) {
						this.fncDAO = new FinanceiroDAO();
						this.listarGedAprovados = this.fncDAO.listarGEDAprovados(this.gerente);
					}
					return this.listarGedAprovados;
				} else {
					return null;
				}
			}
		} else {
			return null;
		}
	}

	public void setListarGedAprovados(List<consulta_financeiro> listarGedAprovados) {
		this.listarGedAprovados = listarGedAprovados;
	}

	public List<consulta_financeiro> getFiltroGedAprovados() {
		return filtroGedAprovados;
	}

	public void setFiltroGedAprovados(List<consulta_financeiro> filtroGedAprovados) {
		this.filtroGedAprovados = filtroGedAprovados;
	}

	public List<consulta_financeiro> getFiltroGed() {
		return filtroGed;
	}

	public void setFiltroGed(List<consulta_financeiro> filtroGed) {
		this.filtroGed = filtroGed;
	}

	public List<cndpagar> getListaDePagar() {
		return listaDePagar;
	}

	public void setListaDePagar(List<cndpagar> listaDePagar) {
		this.listaDePagar = listaDePagar;
	}

	public List<cndpagar> getFiltroDePagar() {
		return filtroDePagar;
	}

	public void setFiltroDePagar(List<cndpagar> filtroDePagar) {
		this.filtroDePagar = filtroDePagar;
	}

	public String getNomeCondominio() {
		return nomeCondominio;
	}

	public void setNomeCondominio(String nomeCondominio) {
		this.nomeCondominio = nomeCondominio;
	}

	public double getValorBruto() {
		return valorBruto;
	}

	public void setValorBruto(double valorBruto) {
		this.valorBruto = valorBruto;
	}

	public boolean isGridTblLancamentos() {
		return gridTblLancamentos;
	}

	public void setGridTblLancamentos(boolean gridTblLancamentos) {
		this.gridTblLancamentos = gridTblLancamentos;
	}

	public boolean isGridDetalhesLancamento() {
		return gridDetalhesLancamento;
	}

	public void setGridDetalhesLancamento(boolean gridDetalhesLancamento) {
		this.gridDetalhesLancamento = gridDetalhesLancamento;
	}

	public int getCdFinancImagem() {
		return cdFinancImagem;
	}

	public void setCdFinancImagem(int cdFinancImagem) {
		this.cdFinancImagem = cdFinancImagem;
	}

	public Date getDtInicioFollowup() {
		return dtInicioFollowup;
	}

	public void setDtInicioFollowup(Date dtInicioFollowup) {
		this.dtInicioFollowup = dtInicioFollowup;
	}

	public Date getDtFimFollowup() {
		return dtFimFollowup;
	}

	public void setDtFimFollowup(Date dtFimFollowup) {
		this.dtFimFollowup = dtFimFollowup;
	}

	public String getNomeDoPlanoContas() {
		return nomeDoPlanoContas;
	}

	public void setNomeDoPlanoContas(String nomeDoPlanoContas) {
		this.nomeDoPlanoContas = nomeDoPlanoContas;
	}

	public String getTipoPagamento() {
		return tipoPagamento;
	}

	public void setTipoPagamento(String tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
	}

	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras(String codigoBarras) {
		this.codigoBarras = codigoBarras;
	}

	public cndpagar getCndpagar() {
		return cndpagar;
	}

	public void setCndpagar(cndpagar cndpagar) {
		this.cndpagar = cndpagar;
	}

	public cndplano getCndplanoNome() {
		return cndplanoNome;
	}

	public void setCndplanoNome(cndplano cndplanoNome) {
		this.cndplanoNome = cndplanoNome;
	}

	public List<String> getListaDeCodReduzido() {
		return listaDeCodReduzido;
	}

	public void setListaDeCodReduzido(List<String> listaDeCodReduzido) {
		this.listaDeCodReduzido = listaDeCodReduzido;
	}

	public String getCodBanco() {
		return codBanco;
	}

	public void setCodBanco(String codBanco) {
		this.codBanco = codBanco;
	}

	public String getNomeDoBanco() {
		return nomeDoBanco;
	}

	public void setNomeDoBanco(String nomeDoBanco) {
		this.nomeDoBanco = nomeDoBanco;
	}

	public String getContaPoupanca() {
		return contaPoupanca;
	}

	public void setContaPoupanca(String contaPoupanca) {
		this.contaPoupanca = contaPoupanca;
	}

	public String getCodAgencia() {
		return codAgencia;
	}

	public void setCodAgencia(String codAgencia) {
		this.codAgencia = codAgencia;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getDac() {
		return dac;
	}

	public void setDac(String dac) {
		this.dac = dac;
	}

	public String getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(String tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public String getCpf_cnpj() {
		return cpf_cnpj;
	}

	public void setCpf_cnpj(String cpf_cnpj) {
		this.cpf_cnpj = cpf_cnpj;
	}

	public String getTipoPagto() {
		return tipoPagto;
	}

	public void setTipoPagto(String tipoPagto) {
		this.tipoPagto = tipoPagto;
	}

	public String getCodMovimento() {
		return codMovimento;
	}

	public void setCodMovimento(String codMovimento) {
		this.codMovimento = codMovimento;
	}

	public String getCodCompensacao() {
		return codCompensacao;
	}

	public void setCodCompensacao(String codCompensacao) {
		this.codCompensacao = codCompensacao;
	}

	public String getLdCampo1() {
		return ldCampo1;
	}

	public void setLdCampo1(String ldCampo1) {
		this.ldCampo1 = ldCampo1;
	}

	public String getLdCampo2() {
		return ldCampo2;
	}

	public void setLdCampo2(String ldCampo2) {
		this.ldCampo2 = ldCampo2;
	}

	public String getLdCampo3() {
		return ldCampo3;
	}

	public void setLdCampo3(String ldCampo3) {
		this.ldCampo3 = ldCampo3;
	}

	public String getLdDac() {
		return ldDac;
	}

	public void setLdDac(String ldDac) {
		this.ldDac = ldDac;
	}

	public String getLdValor() {
		return ldValor;
	}

	public void setLdValor(String ldValor) {
		this.ldValor = ldValor;
	}

	public String getConcSegbarra1() {
		return concSegbarra1;
	}

	public void setConcSegbarra1(String concSegbarra1) {
		this.concSegbarra1 = concSegbarra1;
	}

	public String getConcSegbarra2() {
		return concSegbarra2;
	}

	public void setConcSegbarra2(String concSegbarra2) {
		this.concSegbarra2 = concSegbarra2;
	}

	public String getConcSegbarra3() {
		return concSegbarra3;
	}

	public void setConcSegbarra3(String concSegbarra3) {
		this.concSegbarra3 = concSegbarra3;
	}

	public String getConcSegbarra4() {
		return concSegbarra4;
	}

	public void setConcSegbarra4(String concSegbarra4) {
		this.concSegbarra4 = concSegbarra4;
	}

	public String getCodBarras() {
		return codBarras;
	}

	public void setCodBarras(String codBarras) {
		this.codBarras = codBarras;
	}

	public boolean isUsarTED() {
		return usarTED;
	}

	public void setUsarTED(boolean usarTED) {
		this.usarTED = usarTED;
	}

	public String getLinhaDigitavel() {
		return linhaDigitavel;
	}

	public void setLinhaDigitavel(String linhaDigitavel) {
		this.linhaDigitavel = linhaDigitavel;
	}

	public List<cndpagar_followup> getLstFollowUp() {
		return lstFollowUp;
	}

	public void setLstFollowUp(List<cndpagar_followup> lstFollowUp) {
		this.lstFollowUp = lstFollowUp;
	}

	public List<cndpagar_followup> getLstFollowUpTbl() {
		return lstFollowUpTbl;
	}

	public void setLstFollowUpTbl(List<cndpagar_followup> lstFollowUpTbl) {
		this.lstFollowUpTbl = lstFollowUpTbl;
	}

	public List<cndpagar_followup> getFltrFollowUp() {
		return fltrFollowUp;
	}

	public void setFltrFollowUp(List<cndpagar_followup> fltrFollowUp) {
		this.fltrFollowUp = fltrFollowUp;
	}

	public String[] getAcoes() {
		acoes = new String[5];
		acoes[0] = "Adicionado";
		acoes[1] = "Alterado";
		acoes[2] = "Excluído";
		acoes[3] = "Aprovado";
		acoes[4] = "Protocolo Recebido";
		return acoes;
	}

	public void setAcoes(String[] acoes) {
		this.acoes = acoes;
	}

	public String getObsLancto() {
		return obsLancto;
	}

	public void setObsLancto(String obsLancto) {
		this.obsLancto = obsLancto;
	}

	public List<cndpagar> getLstLancamentos() {
		return lstLancamentos;
	}

	public void setLstLancamentos(List<cndpagar> lstLancamentos) {
		this.lstLancamentos = lstLancamentos;
	}

	public boolean isFlagObs() {
		return flagObs;
	}

	public void setFlagObs(boolean flagObs) {
		this.flagObs = flagObs;
	}

	public cndcondo_param getCondoParam() {
		return condoParam;
	}

	public void setCondoParam(cndcondo_param condoParam) {
		this.condoParam = condoParam;
	}

	public intra_grupo_gerente getGerenteMB() {
		return gerenteMB;
	}

	public void setGerenteMB(intra_grupo_gerente gerenteMB) {
		this.gerenteMB = gerenteMB;
	}

	public List<intra_grupo_gerente> getListaDeGerentes() {
		if (this.listaDeGerentes == null) {
			this.listaDeGerentes = this.retornaGerentes();
		}
		return listaDeGerentes;
	}

	public void setListaDeGerentes(List<intra_grupo_gerente> listaDeGerentes) {
		this.listaDeGerentes = listaDeGerentes;
	}

	public intra_condominios getIcBean() {
		return icBean;
	}

	public void setIcBean(intra_condominios icBean) {
		this.icBean = icBean;
	}

	public List<intra_condominios> getListaDeCondominio() {
		if (this.gerenteMB.getCodigo() > 0) {
			this.fncDAO = new FinanceiroDAO();
			this.listaDeCondominio = this.fncDAO.listarCondominios(this.gerente);
		}
		return listaDeCondominio;
	}

	public void setListaDeCondominio(List<intra_condominios> listaDeCondominio) {
		this.listaDeCondominio = listaDeCondominio;
	}

	public String getNomeCondo() {
		return nomeCondo;
	}

	public void setNomeCondo(String nomeCondo) {
		this.nomeCondo = nomeCondo;
	}

	public boolean isGrid1() {
		return grid1;
	}

	public void setGrid1(boolean grid1) {
		this.grid1 = grid1;
	}

	public boolean isGrid2() {
		return grid2;
	}

	public void setGrid2(boolean grid2) {
		this.grid2 = grid2;
	}

	public boolean isGrid3() {
		return grid3;
	}

	public void setGrid3(boolean grid3) {
		this.grid3 = grid3;
	}

	public boolean isGrid4() {
		return grid4;
	}

	public void setGrid4(boolean grid4) {
		this.grid4 = grid4;
	}

	public String getRateado() {
		return rateado;
	}

	public void setRateado(String rateado) {
		this.rateado = rateado;
	}

	public String getValor1() {
		return valor1;
	}

	public void setValor1(String valor1) {
		this.valor1 = valor1;
	}

	public String getValor2() {
		return valor2;
	}

	public void setValor2(String valor2) {
		this.valor2 = valor2;
	}

	public String getValor3() {
		return valor3;
	}

	public void setValor3(String valor3) {
		this.valor3 = valor3;
	}

	public String getValor4() {
		return valor4;
	}

	public void setValor4(String valor4) {
		this.valor4 = valor4;
	}

	public String getValor5() {
		return valor5;
	}

	public void setValor5(String valor5) {
		this.valor5 = valor5;
	}

	public String getValor6() {
		return valor6;
	}

	public void setValor6(String valor6) {
		this.valor6 = valor6;
	}

	public List<financeiro_imagem> getListaArquivos() {
		return listaArquivos;
	}

	public void setListaArquivos(List<financeiro_imagem> listaArquivos) {
		this.listaArquivos = listaArquivos;
	}

	public CodigoBarras getCodigoBarrasUtil() {
		return codigoBarrasUtil;
	}

	public void setCodigoBarrasUtil(CodigoBarras codigoBarrasUtil) {
		this.codigoBarrasUtil = codigoBarrasUtil;
	}

	public String getIdLancamento() {
		return idLancamento;
	}

	public void setIdLancamento(String idLancamento) {
		this.idLancamento = idLancamento;
	}

	public Date getVencimento() {
		return vencimento;
	}

	public void setVencimento(Date vencimento) {
		this.vencimento = vencimento;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getContaContabil() {
		return contaContabil;
	}

	public void setContaContabil(String contaContabil) {
		this.contaContabil = contaContabil;
	}

	public String getFornecedor() {
		return fornecedor;
	}

	public void setFornecedor(String fornecedor) {
		this.fornecedor = fornecedor;
	}

	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	public String getComplemento1() {
		return complemento1;
	}

	public void setComplemento1(String complemento1) {
		this.complemento1 = complemento1;
	}

	public String getComplemento2() {
		return complemento2;
	}

	public void setComplemento2(String complemento2) {
		this.complemento2 = complemento2;
	}

	public String getComplemento3() {
		return complemento3;
	}

	public void setComplemento3(String complemento3) {
		this.complemento3 = complemento3;
	}

	public String getComplemento4() {
		return complemento4;
	}

	public void setComplemento4(String complemento4) {
		this.complemento4 = complemento4;
	}

	public String getComplemento5() {
		return complemento5;
	}

	public void setComplemento5(String complemento5) {
		this.complemento5 = complemento5;
	}

	public String getComplemento6() {
		return complemento6;
	}

	public void setComplemento6(String complemento6) {
		this.complemento6 = complemento6;
	}

	public String getNotaFiscal() {
		return notaFiscal;
	}

	public void setNotaFiscal(String notaFiscal) {
		this.notaFiscal = notaFiscal;
	}

	public Date getDtEmissaoNF() {
		return dtEmissaoNF;
	}

	public void setDtEmissaoNF(Date dtEmissaoNF) {
		this.dtEmissaoNF = dtEmissaoNF;
	}

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

	public String getFavorecido() {
		return favorecido;
	}

	public void setFavorecido(String favorecido) {
		this.favorecido = favorecido;
	}

	public String getClassificacao() {
		return classificacao;
	}

	public void setClassificacao(String classificacao) {
		this.classificacao = classificacao;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getTipoDocumentoPesquisa() {
		return tipoDocumentoPesquisa;
	}

	public void setTipoDocumentoPesquisa(String tipoDocumentoPesquisa) {
		this.tipoDocumentoPesquisa = tipoDocumentoPesquisa;
	}

	public int getValidaAlterar() {
		return validaAlterar;
	}

	public void setValidaAlterar(int validaAlterar) {
		this.validaAlterar = validaAlterar;
	}

	public int getCodigoConta() {
		return codigoConta;
	}

	public void setCodigoConta(int codigoConta) {
		this.codigoConta = codigoConta;
	}

	public String getNomeConta() {
		return nomeConta;
	}

	public void setNomeConta(String nomeConta) {
		this.nomeConta = nomeConta;
	}

	public String getNomeCapa() {
		return nomeCapa;
	}

	public void setNomeCapa(String nomeCapa) {
		this.nomeCapa = nomeCapa;
	}

	public String getIdImagem() {
		return idImagem;
	}

	public void setIdImagem(String idImagem) {
		this.idImagem = idImagem;
	}

	public String getNomeFornecedor() {
		return nomeFornecedor;
	}

	public void setNomeFornecedor(String nomeFornecedor) {
		this.nomeFornecedor = nomeFornecedor;
	}

	public String getHistoricoExibicaoRateado() {
		return historicoExibicaoRateado;
	}

	public void setHistoricoExibicaoRateado(String historicoExibicaoRateado) {
		this.historicoExibicaoRateado = historicoExibicaoRateado;
	}

	public String getParcelamento() {
		return parcelamento;
	}

	public void setParcelamento(String parcelamento) {
		this.parcelamento = parcelamento;
	}

	public String getPci() {
		return pci;
	}

	public void setPci(String pci) {
		this.pci = pci;
	}

	public String getPcf() {
		return pcf;
	}

	public void setPcf(String pcf) {
		this.pcf = pcf;
	}

	public int getQtdeDeContas() {
		return qtdeDeContas;
	}

	public void setQtdeDeContas(int qtdeDeContas) {
		this.qtdeDeContas = qtdeDeContas;
	}

	public String getCodRed1() {
		return codRed1;
	}

	public void setCodRed1(String codRed1) {
		this.codRed1 = codRed1;
	}

	public String getCodRed2() {
		return codRed2;
	}

	public void setCodRed2(String codRed2) {
		this.codRed2 = codRed2;
	}

	public String getCodRed3() {
		return codRed3;
	}

	public void setCodRed3(String codRed3) {
		this.codRed3 = codRed3;
	}

	public String getCodRed4() {
		return codRed4;
	}

	public void setCodRed4(String codRed4) {
		this.codRed4 = codRed4;
	}

	public String getCodRed5() {
		return codRed5;
	}

	public void setCodRed5(String codRed5) {
		this.codRed5 = codRed5;
	}

	public String getCodRed6() {
		return codRed6;
	}

	public void setCodRed6(String codRed6) {
		this.codRed6 = codRed6;
	}

	public rateio getRat() {
		return rat;
	}

	public void setRat(rateio rat) {
		this.rat = rat;
	}

	public cndplano getCndplano() {
		return cndplano;
	}

	public void setCndplano(cndplano cndplano) {
		this.cndplano = cndplano;
	}

	public int getCondominio() {
		return condominio;
	}

	public void setCondominio(int condominio) {
		this.condominio = condominio;
	}

	public List<cndplano> getLstConta() {
		return lstConta;
	}

	public void setLstConta(List<cndplano> lstConta) {
		this.lstConta = lstConta;
	}

	public intra_grupo_gerente getGerenteBEAN() {
		if (this.listaDeGerentes == null) {
			this.listaDeGerentes = this.retornaGerentes();
		}
		return gerenteBEAN;
	}

	public void setGerenteBEAN(intra_grupo_gerente gerenteBEAN) {
		this.gerenteBEAN = gerenteBEAN;
	}

	public cpcredor getFornecedorSelecionado() {
		return fornecedorSelecionado;
	}

	public void setFornecedorSelecionado(cpcredor fornecedorSelecionado) {
		this.fornecedorSelecionado = fornecedorSelecionado;
	}

	public String getNomeFavorecido() {
		return nomeFavorecido;
	}

	public void setNomeFavorecido(String nomeFavorecido) {
		this.nomeFavorecido = nomeFavorecido;
	}

	public atbancos getBancoSelecionado() {
		return bancoSelecionado;
	}

	public void setBancoSelecionado(atbancos bancoSelecionado) {
		this.bancoSelecionado = bancoSelecionado;
	}

	public List<cpcredor> getLstFornecedor() {
		return lstFornecedor;
	}

	public void setLstFornecedor(List<cpcredor> lstFornecedor) {
		this.lstFornecedor = lstFornecedor;
	}

	public List<cpfavor> getLstFavorecido() {
		return lstFavorecido;
	}

	public void setLstFavorecido(List<cpfavor> lstFavorecido) {
		this.lstFavorecido = lstFavorecido;
	}

	public cpfavor getFavorecidoSelecionado() {
		return favorecidoSelecionado;
	}

	public void setFavorecidoSelecionado(cpfavor favorecidoSelecionado) {
		this.favorecidoSelecionado = favorecidoSelecionado;
	}

	public int getValCodLacto() {
		return valCodLacto;
	}

	public void setValCodLacto(int valCodLacto) {
		this.valCodLacto = valCodLacto;
	}

	public Date getValDatVenc() {
		return valDatVenc;
	}

	public void setValDatVenc(Date valDatVenc) {
		this.valDatVenc = valDatVenc;
	}

	public String getValCodBarras() {
		return valCodBarras;
	}

	public void setValCodBarras(String valCodBarras) {
		this.valCodBarras = valCodBarras;
	}

	public String getValCodAg() {
		return valCodAg;
	}

	public void setValCodAg(String valCodAg) {
		this.valCodAg = valCodAg;
	}

	public String getValCC() {
		return valCC;
	}

	public void setValCC(String valCC) {
		this.valCC = valCC;
	}

	public double getValValor() {
		return valValor;
	}

	public void setValValor(double valValor) {
		this.valValor = valValor;
	}

	public boolean isValValida() {
		return valValida;
	}

	public void setValValida(boolean valValida) {
		this.valValida = valValida;
	}

	public boolean isValidaEtiqueta() {
		return validaEtiqueta;
	}

	public void setValidaEtiqueta(boolean validaEtiqueta) {
		this.validaEtiqueta = validaEtiqueta;
	}

	public String getListarPor() {
		return listarPor;
	}

	public void setListarPor(String listarPor) {
		this.listarPor = listarPor;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public financeiro_imagem getImagemSelecionada() {
		return imagemSelecionada;
	}

	public void setImagemSelecionada(financeiro_imagem imagemSelecionada) {
		this.imagemSelecionada = imagemSelecionada;
	}

	public String getNomeImagem() {
		return nomeImagem;
	}

	public void setNomeImagem(String nomeImagem) {
		this.nomeImagem = nomeImagem;
	}

	public boolean isAlteracao() {
		return alteracao;
	}

	public void setAlteracao(boolean alteracao) {
		this.alteracao = alteracao;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public byte[] getArquivo() {
		return arquivo;
	}

	public void setArquivo(byte[] arquivo) {
		this.arquivo = arquivo;
	}

	public int getGerente() {
		return gerente;
	}

	public void setGerente(int gerente) {
		this.gerente = gerente;
	}

	public StreamedContent getArquivoDownload() {
		return arquivoDownload;
	}

	public void setArquivoDownload(StreamedContent arquivoDownload) {
		this.arquivoDownload = arquivoDownload;
	}

	public List<atbancos> getLstBancos() {
		return lstBancos;
	}

	public void setLstBancos(List<atbancos> lstBancos) {
		this.lstBancos = lstBancos;
	}

	public List<cndpagar> getLstRateioAlteracao() {
		return lstRateioAlteracao;
	}

	public void setLstRateioAlteracao(List<cndpagar> lstRateioAlteracao) {
		this.lstRateioAlteracao = lstRateioAlteracao;
	}

	public List<rateio> getListaDeRateio() {
		return listaDeRateio;
	}

	public void setListaDeRateio(List<rateio> listaDeRateio) {
		this.listaDeRateio = listaDeRateio;
	}

	public List<Integer> getLstExcArquivo() {
		return lstExcArquivo;
	}

	public void setLstExcArquivo(List<Integer> lstExcArquivo) {
		this.lstExcArquivo = lstExcArquivo;
	}

	public List<Integer> getLstNroLancto() {
		return lstNroLancto;
	}

	public void setLstNroLancto(List<Integer> lstNroLancto) {
		this.lstNroLancto = lstNroLancto;
	}

	public List<cndpagar> getListaAprovadosPagar() {
		if (!this.sessaoMB.getUsuario().getGrupoGer().isEmpty()) {
			if (this.sessaoMB.getUsuario().getGrupoGer().get(0).getNome().equals(" Todos")) {
				this.listaAprovadosPagar = null;
				this.filtroAprovadosPagar = null;
				if (this.listaAprovadosPagar == null) {
					this.fncDAO = new FinanceiroDAO();
					this.listaAprovadosPagar = this.fncDAO.listarAprovados();
				}
				return listaAprovadosPagar;
			} else {
				this.listaAprovadosPagar = null;
				this.filtroAprovadosPagar = null;
				if (!this.sessaoMB.getUsuario().getGrupoGer().isEmpty()) {
					if (this.listaAprovadosPagar == null) {
						this.fncDAO = new FinanceiroDAO();
						this.listaAprovadosPagar = this.fncDAO.listarAprovados(this.gerente);
					}
					return listaAprovadosPagar;
				} else {
					return null;
				}
			}
		} else {
			return null;
		}
	}

	public void setListaAprovadosPagar(List<cndpagar> listaAprovadosPagar) {
		this.listaAprovadosPagar = listaAprovadosPagar;
	}

	public List<cndpagar> getFiltroAprovadosPagar() {
		return filtroAprovadosPagar;
	}

	public void setFiltroAprovadosPagar(List<cndpagar> filtroAprovadosPagar) {
		this.filtroAprovadosPagar = filtroAprovadosPagar;
	}

	public List<cndpagar> getListarCndpagarContas() {
		if (this.listarCndpagarContas == null) {
			this.fncDAO = new FinanceiroDAO();
			this.listarCndpagarContas = this.fncDAO.getListaLancamentoContas(this.opcaoLancto,
					this.sessaoMB.getUsuario().getEmail(), this.filtroCndpagarContasInicial,
					this.filtroCndpagarContasFinal);
		}
		return listarCndpagarContas;
	}

	public void setListarCndpagarContas(List<cndpagar> listarCndpagarContas) {
		this.listarCndpagarContas = listarCndpagarContas;
	}

	public List<cndpagar> getListarCndpagarGerente() {
		if (this.listarCndpagarGerente == null) {
			this.listarCndpagarGerente = new ArrayList<>();
			this.fncDAO = new FinanceiroDAO();
			this.listarCndpagarGerente = this.fncDAO.getListaLancamentoGerente(this.opcaoGerente,
					this.sessaoMB.getGerenteSelecionado().getCodigo(), this.filtroCndpagarGerInicial,
					this.filtroCndpagarGerFinal);
			List<Integer> listaCnd = new ArrayList<>();
			for (intra_condominios aux : this.sessaoMB.getListaCondominios()) {
				listaCnd.add(aux.getCodigo());
			}
			// this.listarCndpagarGerente.addAll(this.fncDAO.pesquisaDA(listaCnd));
		}
		return listarCndpagarGerente;
	}

	public void setListarCndpagarGerente(List<cndpagar> listarCndpagarGerente) {
		this.listarCndpagarGerente = listarCndpagarGerente;
	}

	public List<cndpagar> getFiltroCndpagarOma() {
		return filtroCndpagarOma;
	}

	public void setFiltroCndpagarOma(List<cndpagar> filtroCndpagarOma) {
		this.filtroCndpagarOma = filtroCndpagarOma;
	}

	public boolean isProxima1() {
		return proxima1;
	}

	public void setProxima1(boolean proxima1) {
		this.proxima1 = proxima1;
	}

	public boolean isProxima2() {
		return proxima2;
	}

	public void setProxima2(boolean proxima2) {
		this.proxima2 = proxima2;
	}

	public int getCdImagem() {
		return cdImagem;
	}

	public void setCdImagem(int cdImagem) {
		this.cdImagem = cdImagem;
	}

	public String getCodigoHistPadrao() {
		return codigoHistPadrao;
	}

	public void setCodigoHistPadrao(String codigoHistPadrao) {
		this.codigoHistPadrao = codigoHistPadrao;
	}

	public String getMsgHistPadrao() {
		return msgHistPadrao;
	}

	public void setMsgHistPadrao(String msgHistPadrao) {
		this.msgHistPadrao = msgHistPadrao;
	}

	public double getValorGed() {
		return valorGed;
	}

	public void setValorGed(double valorGed) {
		this.valorGed = valorGed;
	}

	public int getHideSalvar() {
		return hideSalvar;
	}

	public void setHideSalvar(int hideSalvar) {
		this.hideSalvar = hideSalvar;
	}

	public List<cndpagar> getSelectCndpagar() {
		return selectCndpagar;
	}

	public void setSelectCndpagar(List<cndpagar> selectCndpagar) {
		this.selectCndpagar = selectCndpagar;
	}

	public List<cndpagar> getListaAlteracaoContas() {
		if (this.listaAlteracaoContas == null) {
			this.fncDAO = new FinanceiroDAO();
			this.dataInicio = new DateTime().plusDays(1).withMillisOfDay(0).toDate();
			this.dataFim = new DateTime().plusDays(1).withMillisOfDay(0).toDate();
			this.listaAlteracaoContas = this.fncDAO.getListaAlteracaoContas(this.dataInicio, this.dataFim,
					this.codigoConta);
		}
		return listaAlteracaoContas;
	}

	public void setListaAlteracaoContas(List<cndpagar> listaAlteracaoContas) {
		this.listaAlteracaoContas = listaAlteracaoContas;
	}

	public List<sgimpos> getListaDeTributos() {
		return listaDeTributos;
	}

	public void setListaDeTributos(List<sgimpos> listaDeTributos) {
		this.listaDeTributos = listaDeTributos;
	}

	public List<cndpagar> getListaDeDuplicidade() {
		return listaDeDuplicidade;
	}

	public void setListaDeDuplicidade(List<cndpagar> listaDeDuplicidade) {
		this.listaDeDuplicidade = listaDeDuplicidade;
	}

	// METODOS
	public List<intra_grupo_gerente> retornaGerentes() {
		if (!this.sessaoMB.getUsuario().getGrupoGer().isEmpty()) {
			if (this.sessaoMB.getUsuario().getGrupoGer().get(0).getNome().equals(" Todos")) {
				this.listarGed = null;
				this.filtroDePagar = null;
				this.gerenteMB.setCodigo(this.sessaoMB.getListaDeGerente().get(0).getCodigo());
				this.gerente = this.gerenteMB.getCodigo();
				return this.sessaoMB.getListaDeGerente();
			} else {
				this.listarGed = null;
				this.filtroDePagar = null;
				if (!this.sessaoMB.getUsuario().getGrupoGer().isEmpty()) {
					this.gerenteMB.setCodigo(this.sessaoMB.getUsuario().getGrupoGer().get(0).getCodigo());
					this.gerente = this.gerenteMB.getCodigo();
					return this.sessaoMB.getUsuario().getGrupoGer();
				} else {
					return null;
				}
			}
		} else {
			return null;
		}
	}

	public void retornaNomeCondominio() {
		for (intra_condominios c : sessaoMB.getListaDeCondominios()) {
			if (c.getCodigo() == this.condominio) {
				this.nomeCondo = c.getNome();
				this.icBean.setNomeGerente(c.getNomeGerente());
				this.icBean.setEmailGerente(c.getEmailGerente());
				this.icBean.setCodigoGerente(c.getCodigoGerente());
			}
		}
	}

	public void listarContas(Short condominio) {
		this.listaDePagar = null;
		this.nomeCondominio = "";
		this.condominio = condominio;
		this.fncDAO = new FinanceiroDAO();
		if (this.sessaoMB.getUsuario().getGrupoDepto().get(0).getNome().equals("Contas a Pagar")) {
			this.listaDePagar = this.fncDAO.listarCndPagarContas(condominio);
		} else {
			this.listaDePagar = this.fncDAO.listarCndPagar(condominio);
		}
		this.nomeCondominio = this.fncDAO.listarNomeCondominio(condominio);
	}

	public void listarContasAprovadas(Short condominio) {
		this.listaDePagar = null;
		this.nomeCondominio = "";
		this.condominio = condominio;
		this.fncDAO = new FinanceiroDAO();
		this.listaDePagar = this.fncDAO.listarCndPagarAprovados(condominio);
		this.nomeCondominio = this.fncDAO.listarNomeCondominio(condominio);
	}

	public void abrirLancamento(cndpagar cndpagar) {
		this.valorBruto = 0.0;
		this.fncDAO = new FinanceiroDAO();
		this.cndpagar = this.fncDAO.pesqLancto(cndpagar.getCodigo());
		this.gridTblLancamentos = false;
		this.gridDetalhesLancamento = true;
		this.cdFinancImagem = 0;
		this.nomeDoPlanoContas = "";
		if (this.cndpagar.getTipoPagto().equals("8") || this.cndpagar.getTipoPagto().equals("E")) {
			this.tipoPagamento = this.cndpagar.getTipoPagto();
			this.codigoBarras = this.cndpagar.getCodigoBarra();
			this.listarLinhaDigitavel();
		}
		this.nomePlanoContas(cndpagar);

		if (this.cndpagar.getRateado().equals("S")) {
			this.listaDeCodReduzido = new ArrayList<>();
			this.fncDAO = new FinanceiroDAO();
			List<cndpagar> lst = this.fncDAO.listarContasRateadas(this.cndpagar.getCodigoRateio());
			DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));
			for (cndpagar aux : lst) {
				this.cndplanoNome = new cndplano();
				this.nomePlanoContas(aux);
				String contaValor = "Conta: " + (aux.getConta() > 100000 ? aux.getConta() - 100000 : aux.getConta())
						+ "-" + this.cndplanoNome.getNome() + " - R$ " + df.format(aux.getValor());
				this.listaDeCodReduzido.add(contaValor);
				this.valorBruto += aux.getValor();
			}
		}

		if (this.cndpagar.isAguardandoCompletarResumido()) {
			this.abrirGridCompletarAprovacao();
		}
	}

	public void nomePlanoContas(cndpagar pg) {
		this.fncDAO = new FinanceiroDAO();
		this.cndplanoNome = this.fncDAO.listarPlano(pg.getConta(), pg.getCondominio());
		nomeDoPlanoContas = cndplanoNome.getCod_reduzido() + " - " + cndplanoNome.getNome();
	}

	public boolean listarLinhaDigitavel() {
		boolean sucesso = false;
		if (this.codigoBarras == null || this.codigoBarras.trim().isEmpty()) {
			this.msgCodigoBarraInserir();
			UIComponent component = null;
			if (this.tipoPagamento.equals("8")) {
				component = FacesContext.getCurrentInstance().getViewRoot().findComponent("frmLancamento:txtCodBarras");
				((UIInput) component).setValid(false);
			}
			if (this.tipoPagamento.equals("E")) {
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:txtCodBarras2");
				((UIInput) component).setValid(false);
			}
		} else {
			this.codigoBarrasUtil = new CodigoBarras();
			String linha = this.codigoBarras;
			this.ldCampo1 = "";
			this.ldCampo2 = "";
			this.ldCampo3 = "";
			this.ldDac = "";
			this.ldValor = "";
			this.concSegbarra1 = "";
			this.concSegbarra2 = "";
			this.concSegbarra3 = "";
			this.concSegbarra4 = "";
			try {
				if (linha.length() == 44) {
					if (this.tipoPagamento.equals("8")) {
						String l = linha.substring(0, 3);
						if (l.equals("033")) {
							this.linhaDigitavel = this.validaCodBarras("8", linha);
							this.ldCampo1 = this.linhaDigitavel.substring(0, 11);
							this.ldCampo2 = this.linhaDigitavel.substring(11, 23);
							this.ldCampo3 = this.linhaDigitavel.substring(23, 35);
							this.ldDac = this.linhaDigitavel.substring(35, 36);
							this.ldValor = this.linhaDigitavel.substring(36, 50);
						} else {
							this.linhaDigitavel = this.validaCodBarras("1", linha);
							this.ldCampo1 = this.linhaDigitavel.substring(0, 11);
							this.ldCampo2 = this.linhaDigitavel.substring(11, 23);
							this.ldCampo3 = this.linhaDigitavel.substring(23, 35);
							this.ldDac = this.linhaDigitavel.substring(35, 36);
							this.ldValor = this.linhaDigitavel.substring(36, 50);
						}
						sucesso = false;
						UIComponent component = FacesContext.getCurrentInstance().getViewRoot()
								.findComponent("frmLancamento:txtCodBarras");
						if (component != null) {
							((UIInput) component).setValid(true);
						}
					} else if (this.tipoPagamento.equals("E")) {
						this.linhaDigitavel = this.validaCodBarras("E", linha);
						this.concSegbarra1 = this.linhaDigitavel.substring(0, 12);
						this.concSegbarra2 = this.linhaDigitavel.substring(12, 24);
						this.concSegbarra3 = this.linhaDigitavel.substring(24, 36);
						this.concSegbarra4 = this.linhaDigitavel.substring(36, 48);
					}
					UIComponent component = FacesContext.getCurrentInstance().getViewRoot()
							.findComponent("frmLancamento:txtCodBarras2");
					if (component != null) {
						((UIInput) component).setValid(true);
					}
					sucesso = true;
				} else {
					sucesso = false;
					this.msgCodigoBarraIncorreto();
				}
			} catch (Exception e) {
				sucesso = false;
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage(), e.getMessage()));
			}
		}
		return sucesso;
	}

	public String validaCodBarras(String tipo, String cb) throws RNException {
		switch (tipo) {
		case "8":
			if (!cb.subSequence(0, 1).equals("8")) {
				this.codigoBarrasUtil = new CodigoBarras();
				return codigoBarrasUtil.calculaLinhaBBBrCaIt(cb);
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN,
								"O tipo de pagamento é diferente do codigo de barras!",
								"O tipo de pagamento é diferente do codigo de barras!"));
				break;
			}
		case "1":
			if (!cb.subSequence(0, 1).equals("8")) {
				this.codigoBarrasUtil = new CodigoBarras();
				return codigoBarrasUtil.calculaLinhaBBBrCaIt(cb);
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN,
								"O tipo de pagamento é diferente do codigo de barras!",
								"O tipo de pagamento é diferente do codigo de barras!"));
				break;
			}
		case "E":
			if (cb.subSequence(0, 1).equals("8")) {
				this.codigoBarrasUtil = new CodigoBarras();
				return codigoBarrasUtil.calculaConcessionaria(cb);
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN,
								"O tipo de pagamento é diferente do codigo de barras!",
								"O tipo de pagamento é diferente do codigo de barras!"));
				break;
			}
		}
		return null;
	}

	public String converte(long value) {
		BigInteger teste = BigInteger.valueOf(value);
		return teste.toString();
	}

	public String converteDouble(long i) {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(0);
		return df.format(i);
	}

	public String converteLDConcessionaria(long value) {
		BigInteger teste = BigInteger.valueOf(value);
		String retorno = "";
		if (teste.toString().length() < 12) {
			int zeros = 12 - teste.toString().length();
			for (int i = 0; i < zeros; i++) {
				retorno = retorno + "0";
			}
			retorno = retorno + teste.toString();
		} else {
			retorno = teste.toString();
		}
		return retorno;
	}

	public String converteLdCampo1(double ldCampo1) {
		String retorno = String.valueOf(ldCampo1);
		if (retorno.toString().length() < 11) {
			int zeros = 11 - retorno.toString().length();
			for (int i = 0; i < zeros; i++) {
				retorno = retorno + "0";
			}
			retorno = retorno + retorno.toString();
		} else {
			retorno = retorno.toString();
		}
		return retorno;
	}

	public String converteLdCampo2(double ldCampo2) {
		String retorno = String.valueOf(ldCampo2);
		if (retorno.toString().length() < 12) {
			int zeros = 12 - retorno.toString().length();
			for (int i = 0; i < zeros; i++) {
				retorno = retorno + "0";
			}
			retorno = retorno + retorno.toString();
		} else {
			retorno = retorno.toString();
		}
		return retorno;
	}

	public String converteLdCampo3(double ldCampo3) {
		String retorno = String.valueOf(ldCampo3);
		if (retorno.toString().length() < 12) {
			int zeros = 12 - retorno.toString().length();
			for (int i = 0; i < zeros; i++) {
				retorno = retorno + "0";
			}
			retorno = retorno + retorno.toString();
		} else {
			retorno = retorno.toString();
		}
		return retorno;
	}

	public String converteLdValor(double ldValor) {
		String retorno = String.valueOf(ldCampo3);
		if (retorno.toString().length() < 1) {
			int zeros = 1 - retorno.toString().length();
			for (int i = 0; i < zeros; i++) {
				retorno = retorno + "0";
			}
			retorno = retorno + retorno.toString();
		} else {
			retorno = retorno.toString();
		}
		return retorno;
	}

	public String converteLdDac(double ldDac) {
		String retorno = String.valueOf(ldCampo3);
		if (retorno.toString().length() < 14) {
			int zeros = 14 - retorno.toString().length();
			for (int i = 0; i < zeros; i++) {
				retorno = retorno + "0";
			}
			retorno = retorno + retorno.toString();
		} else {
			retorno = retorno.toString();
		}
		return retorno;
	}

	public void abrirFollowUp() {
		this.fncDAO = new FinanceiroDAO();
		this.lstFollowUp = this.fncDAO.listarFollowUp(this.cndpagar.getCodigo());
		if (this.lstFollowUp.isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Não há nenhum Follow Up para este lançamento!", "Não há nenhum Follow Up para este lançamento!"));
		} else {
			RequestContext.getCurrentInstance().execute("PF('dlgFollowUp').show();");
		}
	}

	public void abrirFollowUp(cndpagar p) {
		this.cndpagar = p;
		this.fncDAO = new FinanceiroDAO();
		this.lstFollowUp = this.fncDAO.listarFollowUp(this.cndpagar.getCodigo());
		if (this.lstFollowUp.isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Não há nenhum Follow Up para este lançamento!", "Não há nenhum Follow Up para este lançamento!"));
		} else {
			RequestContext.getCurrentInstance().execute("PF('dlgFollowUp').show();");
		}
	}

	public boolean validaBtnParcelado() {
		boolean valida = true;
		if (this.cndpagar.isValidaParcelado()) {
			if (this.cndpagar.getPcInicial() < this.cndpagar.getPcFinal()) {
				valida = false;
			}
		}
		return valida;
	}

	public void reprovarLancamento() {
		this.fncDAO = new FinanceiroDAO();
		this.cndpagar.setUsuarioAprovacao(null);
		if (this.cndpagar != null && this.cndpagar.getCodigo() > 0) {
			if (this.cndpagar.getMotivoReprovacao() == null || this.cndpagar.getMotivoReprovacao().equals("")) {
				this.msgMotivoReprovar();
			} else {
				this.obsLancto = this.cndpagar.getMotivoReprovacao();

				if (this.obsLancto != null && !this.obsLancto.isEmpty()) {
					this.cndpagar.setObs("S");
				}

				if (this.cndpagar.getCodigoRateio() > 0 && this.cndpagar.getRateado().equals("S")) {
					List<cndpagar> lista = this.fncDAO.listaDeCndpagarRateado(this.cndpagar);
					for (cndpagar aux : lista) {
						List<cndpagar_aprovacao> listaAprovadores = new ArrayList<cndpagar_aprovacao>();
						listaAprovadores.addAll(aux.getAprovadores());
						aux.getAprovadores().clear();
						this.fncDAO.atualizaAprovacaoLancamento(aux, null, this.sessaoMB.getUsuario().getEmail(), "oma",
								this.obsLancto);
					}
				} else {
					List<cndpagar_aprovacao> listaAprovadores = new ArrayList<cndpagar_aprovacao>();
					listaAprovadores.addAll(this.cndpagar.getAprovadores());
					this.cndpagar.getAprovadores().clear();
					this.fncDAO.atualizaAprovacaoLancamento(this.cndpagar, null, this.sessaoMB.getUsuario().getEmail(),
							"oma", this.obsLancto);
					this.fncDAO.removerAprovacoes(listaAprovadores);
				}

				this.fncDAO.reprovarLancamento(this.cndpagar, this.sessaoMB.getUsuario().getEmail(), "oma",
						this.obsLancto);
				RequestContext.getCurrentInstance().execute("PF('dlgReprovacao').hide();");
				this.listaDePagar = null;
				this.filtroDePagar = null;
				this.listarGed = null;

				this.cdFinancImagem = 0;
				this.obsLancto = null;
				try {
					EnvioEmail envio = new EnvioEmail();
					envio.reprovacaoLancamento(this.cndpagar);
				} catch (RNException e) {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage(), ""));
				}
				// this.limparFiltros();
				this.msgReprovaLancto();
			}
		} else {
			this.msgSelectLancto();
		}
	}

	public void excluirLancamento() {
		this.fncDAO = new FinanceiroDAO();
		if (this.cndpagar != null && this.cndpagar.getCodigo() > 0) {
			List<cndpagar_aprovacao> listaAprovadores = null;

			if (this.cndpagar.getCodigoRateio() > 0 && this.cndpagar.getRateado().equals("S")) {
				List<cndpagar> lista = fncDAO.listaDeCndpagarRateado(this.cndpagar);
				for (cndpagar aux : lista) {
					listaAprovadores = new ArrayList<cndpagar_aprovacao>();
					listaAprovadores.addAll(aux.getAprovadores());
				}
			} else {
				listaAprovadores = new ArrayList<cndpagar_aprovacao>();
				listaAprovadores.addAll(this.cndpagar.getAprovadores());
			}

			this.fncDAO.excluirLancamento(this.cndpagar, this.sessaoMB.getUsuario().getEmail(), "oma", this.obsLancto);
			this.fncDAO.removerAprovacoes(listaAprovadores);

			RequestContext.getCurrentInstance().execute("PF('dlgExclui').hide();");
			this.listaDePagar = null;
			this.filtroDePagar = null;
			this.listarGed = null;
			this.listarGed = null;
			this.cdFinancImagem = 0;
			this.obsLancto = null;
			this.msgExclusao();
		} else {
			this.msgRegistro();
		}
	}

	public boolean autorizaAprovacaoSiga() throws RNException {
		boolean autorizado = false;
		this.fncDAO = new FinanceiroDAO();
		if (this.cndpagar != null) {
			this.condoParam = this.fncDAO.condominioParam(Short.valueOf(cndpagar.getCondominio()));
			if (this.condoParam != null && this.condoParam.isAprovacaoLancamentos()) {
				int qtdAprovacoes = 0;
				if (this.condoParam.getNivelAprovacao1() > 0) {
					qtdAprovacoes++;
				}
				if (this.condoParam.getNivelAprovacao2() > 0) {
					qtdAprovacoes++;
				}
				if (this.condoParam.getNivelAprovacao3() > 0) {
					qtdAprovacoes++;
				}
				if (this.condoParam.getNivelAprovacao4() > 0) {
					qtdAprovacoes++;
				}
				if (this.condoParam.getNivelAprovacao5() > 0) {
					qtdAprovacoes++;
				}
				if (this.cndpagar.getAprovadores().size() == qtdAprovacoes) {
					autorizado = true;
				} else {
					autorizado = false;
				}
				for (cndpagar_aprovacao aux : this.cndpagar.getAprovadores()) {
					if (!aux.getStatus_().equals("A")) {
						autorizado = false;
					}
				}
			} else {
				autorizado = true;
			}
		}
		return autorizado;
	}

	public boolean autorizaPreAprovacao() throws RNException {
		boolean autorizado = true;
		this.fncDAO = new FinanceiroDAO();
		if (this.cndpagar != null) {
			intra_grupo_permissao igp = new intra_grupo_permissao();
			for (intra_grupo_permissao p : this.sessaoMB.getUsuario().getGrupoPer()) {
				igp = p;
			}
			if (!igp.isPreAprovarLancto()) {
				autorizado = false;
			} else {
				boolean jaAprovou = false;
				for (cndpagar_aprovacao aux : this.cndpagar.getAprovadores()) {
					if (aux.getAprovador().equals(this.sessaoMB.getUsuario().getEmail())) {
						jaAprovou = true;
					}
				}
				if (jaAprovou) {
					autorizado = false;
				}
			}
		}
		return autorizado;
	}

	public boolean retornaAprovacaoLancto() {
		this.condoParam = new cndcondo_param();
		if (this.cndpagar != null) {
			this.condoParam = this.fncDAO.condominioParam(Short.valueOf(this.cndpagar.getCondominio()));
			return condoParam.isAprovacaoLancamentos();
		} else {
			return false;
		}
	}

	public void addLanctoSigaRateio() {
		this.fncDAO = new FinanceiroDAO();
		String val = "";
		cndpagar cdp = new cndpagar();
		List<cndpagar> lista = this.fncDAO.listaDeCndpagarRateado(this.cndpagar);
		if (this.obsLancto != null && !this.obsLancto.isEmpty()) {
			this.cndpagar.setObs("S");
		}
		for (cndpagar cp : lista) {
			if (this.cndpagar.getCondominio() == 4241) {
				short contaGrau1 = this.fncDAO.listarContaBancaria(cp.getConta());
				cp.setContaBancaria(contaGrau1);
			}
			val = this.fncDAO.adicionarLanctoSigaRateado(cp, this.sessaoMB.getUsuario().getEmail(), "oma",
					this.obsLancto);
			cdp = cp;
			for (int i = 0; i < this.listaDePagar.size(); i++) {
				if (this.listaDePagar.get(i).getCodigo() == cp.getCodigo()) {
					this.listaDePagar.remove(i);
				}
			}
		}

		if (cdp != null) {
			if (cdp.getTipoPagto().equals("5") || cdp.getTipoPagto().equals("7")) {
				if (cdp.getCodigoFav() == 0) {
					this.fncDAO.salvarFavSiga(cdp);
				}
			}
		}

		this.lstLancamentos = null;
		this.gridDetalhesLancamento = false;
		this.gridTblLancamentos = true;
		this.cdFinancImagem = 0;
		this.obsLancto = null;
		this.listaDePagar = null;
		this.filtroDePagar = null;
		this.listarGed = null;
		if (val.equals("A")) {
			this.msgAprovado();
		} else if (val.equals("R")) {
			this.msgErroLanctoAprovado();
		} else {
			this.msgErroLanctoNaoAprovado();
		}

	}

	public void adicionarLancamentoSiga() {
		try {
			this.fncDAO = new FinanceiroDAO();
			FinanceiroSIPDAO sipDAO = new FinanceiroSIPDAO();

			if (this.obsLancto != null && !this.obsLancto.isEmpty()) {
				this.cndpagar.setObs("S");
			}

			if (this.cndpagar.isAguardandoCompletarResumido()) {
				this.constroiLanctoAprovacaoResumido();
			}

			if (this.cndpagar.isAguardandoCompletarResumido() && !this.verificaCamposCompletar()) {
				throw new Exception("Insira todos os campos obrigatórios antes da aprovação!");
			} else {
				this.cndpagar.setAguardandoCompletarResumido(false);
				this.fncDAO.alterarLancamentoResumido(this.cndpagar);
			}
			if (this.cndpagar.getCondominio() == 4241) {
				short contaGrau1 = this.fncDAO.listarContaBancaria(this.cndpagar.getConta());
				this.cndpagar.setContaBancaria(contaGrau1);
			}

			String val = this.fncDAO.adicionarLanctoSiga(this.cndpagar, sessaoMB.getUsuario().getEmail(), "oma",
					this.obsLancto);
			sipDAO.adicionarLanctoSiga(this.cndpagar, sessaoMB.getUsuario().getEmail(), "oma", this.obsLancto);

			for (int i = 0; i < this.listaDePagar.size(); i++) {
				if (this.listaDePagar.get(i).getCodigo() == this.cndpagar.getCodigo()) {
					this.listaDePagar.remove(i);
				}
			}

			this.lstLancamentos = null;
			this.gridDetalhesLancamento = false;
			this.gridTblLancamentos = true;
			this.cdFinancImagem = 0;
			this.obsLancto = null;
			// this.listaDePagar = null;
			// this.filtroDePagar = null;
			this.listarGed = null;
			if (val.equals("A")) {
				this.msgAprovado();
			} else if (val.equals("R")) {
				this.msgTenteNovamente();
			} else {
				this.msgErroLanctoNaoAprovado();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, e.getMessage(), e.getMessage()));
			e.printStackTrace();
		}
	}

	public void aprovaLancamento() throws RNException {
		this.cndpagar.setUsuarioAprovacao(null);
		this.condoParam = new cndcondo_param();
		this.condoParam = this.fncDAO.condominioParam(Short.valueOf(this.cndpagar.getCondominio()));
		intra_grupo_permissao igp = new intra_grupo_permissao();
		for (intra_grupo_permissao p : this.sessaoMB.getUsuario().getGrupoPer()) {
			igp = p;
		}
		int permissao = igp.isAprovarLancto() ? 1 : 0;

		if (this.condoParam.getNivelAprovacao1() != permissao && this.condoParam.getNivelAprovacao2() != permissao
				&& this.condoParam.getNivelAprovacao3() != permissao
				&& this.condoParam.getNivelAprovacao4() != permissao
				&& this.condoParam.getNivelAprovacao5() != permissao) {
			this.msgSemPermissao();
		} else {
			boolean jaAprovou = false;
			for (cndpagar_aprovacao aux : this.cndpagar.getAprovadores()) {
				if (aux.getAprovador() == this.sessaoMB.getUsuario().getEmail()) {
					jaAprovou = true;
					this.msgJaAprovou();
				}
			}

			if (!jaAprovou) {

				int qtdAprovacoes = 0;
				if (this.condoParam.getNivelAprovacao1() > 0) {
					qtdAprovacoes++;
				}
				if (this.condoParam.getNivelAprovacao2() > 0) {
					qtdAprovacoes++;
				}
				if (this.condoParam.getNivelAprovacao3() > 0) {
					qtdAprovacoes++;
				}
				if (this.condoParam.getNivelAprovacao4() > 0) {
					qtdAprovacoes++;
				}
				if (this.condoParam.getNivelAprovacao5() > 0) {
					qtdAprovacoes++;
				}

				if (qtdAprovacoes == this.cndpagar.getAprovadores().size() + 1) {
					this.cndpagar.setStatus("P");
				}

				this.fncDAO = new FinanceiroDAO();
				cndpagar_aprovacao aprovacao = new cndpagar_aprovacao();
				aprovacao.setData(new Date());
				aprovacao.setAprovador(this.sessaoMB.getUsuario().getEmail());
				aprovacao.setPermissao(2);
				aprovacao.setStatus_("A");

				if (this.cndpagar.isAguardandoCompletarResumido()) {
					this.constroiLanctoAprovacaoResumido();
					if (this.verificaCamposCompletar()) {
						this.cndpagar.setAguardandoCompletarResumido(false);
					}
				}

				this.fncDAO.atualizaAprovacaoLancamento(this.cndpagar, aprovacao, this.sessaoMB.getUsuario().getEmail(),
						"oma", this.obsLancto);
				this.msgAprovado();
				this.cndpagar = null;
				this.lstLancamentos = null;
				this.gridDetalhesLancamento = false;
				this.gridTblLancamentos = true;
				this.cdFinancImagem = 0;
				this.obsLancto = null;
				this.listaDePagar = null;
				this.filtroDePagar = null;
				this.listarGed = null;
			}
		}
	}

	public void aprovaLancamentoRateado() throws RNException {
		this.cndpagar.setUsuarioAprovacao(null);
		this.condoParam = new cndcondo_param();
		this.condoParam = this.fncDAO.condominioParam(Short.valueOf(this.cndpagar.getCondominio()));
		intra_grupo_permissao igp = new intra_grupo_permissao();
		for (intra_grupo_permissao p : this.sessaoMB.getUsuario().getGrupoPer()) {
			igp = p;
		}
		int permissao = igp.isAprovarLancto() ? 1 : 0;

		if (this.condoParam.getNivelAprovacao1() != permissao && this.condoParam.getNivelAprovacao2() != permissao
				&& this.condoParam.getNivelAprovacao3() != permissao
				&& this.condoParam.getNivelAprovacao4() != permissao
				&& this.condoParam.getNivelAprovacao5() != permissao) {
			this.msgSemPermissao();
		} else {
			boolean jaAprovou = false;
			for (cndpagar_aprovacao aux : this.cndpagar.getAprovadores()) {
				if (aux.getAprovador() == this.sessaoMB.getUsuario().getEmail()) {
					jaAprovou = true;
					this.msgJaAprovou();
				}
			}
			if (!jaAprovou) {

				this.fncDAO = new FinanceiroDAO();
				List<cndpagar> lista = this.fncDAO.listaDeCndpagarRateado(this.cndpagar);
				cndpagar_aprovacao aprovacao = new cndpagar_aprovacao();
				aprovacao.setData(new Date());
				aprovacao.setAprovador(this.sessaoMB.getUsuario().getEmail());
				aprovacao.setPermissao(2);
				aprovacao.setStatus_("A");
				for (cndpagar aux : lista) {

					int qtdAprovacoes = 0;
					if (this.condoParam.getNivelAprovacao1() > 0) {
						qtdAprovacoes++;
					}
					if (this.condoParam.getNivelAprovacao2() > 0) {
						qtdAprovacoes++;
					}
					if (this.condoParam.getNivelAprovacao3() > 0) {
						qtdAprovacoes++;
					}
					if (this.condoParam.getNivelAprovacao4() > 0) {
						qtdAprovacoes++;
					}
					if (this.condoParam.getNivelAprovacao5() > 0) {
						qtdAprovacoes++;
					}

					if (qtdAprovacoes == this.cndpagar.getAprovadores().size() + 1) {
						aux.setStatus("P");
					}

					this.fncDAO.atualizaAprovacaoLancamento(aux, aprovacao, this.sessaoMB.getUsuario().getEmail(),
							"oma", this.obsLancto);
				}
				this.msgAprovado();
				this.cndpagar = null;
				this.lstLancamentos = null;
				this.gridDetalhesLancamento = false;
				this.gridTblLancamentos = true;
				this.cdFinancImagem = 0;
				this.obsLancto = null;
				this.listaDePagar = null;
				this.filtroDePagar = null;
				this.listarGed = null;
			}
		}
	}

	// ******************************** LANÇAMENTO / ALTERÇÃO
	// **************************//
	public void limparRateio() {
		this.contaContabil = "";
		this.valor = "";
		this.valor1 = "";
		this.valor2 = "";
		this.valor3 = "";
		this.valor4 = "";
		this.valor5 = "";
		this.valor6 = "";
		this.codRed1 = "";
		this.codRed2 = "";
		this.codRed3 = "";
		this.codRed4 = "";
		this.codRed5 = "";
		this.codRed6 = "";
		this.nomeConta = "";
		this.rat.setValorBruto(0);
		this.qtdeDeContas = 0;
	}

	public void validaGrid1() {
		if (this.cndplano == null || this.cndplano.getCod_reduzido() == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Insira uma conta válida!", "Insira uma conta válida!"));
			UIComponent component = FacesContext.getCurrentInstance().getViewRoot()
					.findComponent("frmLancamento:txtContaContabil");
			((UIInput) component).setValid(false);
		} else if (this.fornecedorSelecionado == null) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"CPF/CNPJ inválido ou não cadastrado!", "CPF/CNPJ inválido ou não cadastrado!"));
			UIComponent component = FacesContext.getCurrentInstance().getViewRoot()
					.findComponent("frmLancamento:txtFornecedor");
			((UIInput) component).setValid(false);
		} else if (this.condominio < 110) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Selecione o Condomínio!", "Selecione o Condomínio!"));
			UIComponent component = FacesContext.getCurrentInstance().getViewRoot()
					.findComponent("frmCondominio:console");
			((UIInput) component).setValid(false);
			this.grid2 = false;
		} else {
			this.grid1 = false;
			this.grid2 = true;
		}
	}

	public void somaValor() throws ParseException {
		DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));

		this.valorBruto = 0;
		this.rat.setValorBruto(0);
		this.qtdeDeContas = 0;

		if (this.valor1 != null) {
			this.valor1 = this.valor1.replace("R$ ", "");
			if (!this.valor1.trim().isEmpty() & !this.valor1.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor1).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor1(df.parse(this.valor1).doubleValue());
				qtdeDeContas += 1;
				this.cndpagar.setConta(qtdeDeContas);
			} else if (this.valor1.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor1).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor1(df.parse(this.valor1).doubleValue());
			}
		}
		if (this.valor2 != null) {
			this.valor2 = this.valor2.replace("R$ ", "");
			if (!this.valor2.trim().isEmpty() & !this.valor2.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor2).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor2(df.parse(this.valor2).doubleValue());
				qtdeDeContas += 1;
				this.cndpagar.setConta(qtdeDeContas);
			} else if (this.valor2.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor2).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor2(df.parse(this.valor2).doubleValue());
			}
		}
		if (this.valor3 != null) {
			this.valor3 = this.valor3.replace("R$ ", "");
			if (!this.valor3.trim().isEmpty() & !this.valor3.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor3).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor3(df.parse(this.valor3).doubleValue());
				qtdeDeContas += 1;
				this.cndpagar.setConta(qtdeDeContas);
			} else if (this.valor3.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor3).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor3(df.parse(this.valor3).doubleValue());
			}
		}
		if (this.valor4 != null) {
			this.valor4 = this.valor4.replace("R$ ", "");
			if (!this.valor4.trim().isEmpty() & !this.valor4.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor4).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor4(df.parse(this.valor4).doubleValue());
				qtdeDeContas += 1;
				this.cndpagar.setConta(qtdeDeContas);
			} else if (this.valor4.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor4).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor4(df.parse(this.valor4).doubleValue());
			}
		}
		if (this.valor5 != null) {
			this.valor5 = this.valor5.replace("R$ ", "");
			if (!this.valor5.trim().isEmpty() & !this.valor5.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor5).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor5(df.parse(this.valor5).doubleValue());
				qtdeDeContas += 1;
				this.cndpagar.setConta(qtdeDeContas);
			} else if (this.valor5.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor5).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor5(df.parse(this.valor5).doubleValue());
			}
		}
		if (this.valor6 != null) {
			this.valor6 = this.valor6.replace("R$ ", "");
			if (!this.valor6.trim().isEmpty() & !this.valor6.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor6).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor6(df.parse(this.valor6).doubleValue());
				qtdeDeContas += 1;
				this.cndpagar.setConta(qtdeDeContas);
			} else if (this.valor6.equals("0,00")) {
				this.rat.setValorBruto(df.parse(this.valor6).doubleValue() + this.rat.getValorBruto());
				this.rat.setValor6(df.parse(this.valor6).doubleValue());
			}
		}
	}

	public void pesquisaContaRatCod1() {
		this.fncDAO = new FinanceiroDAO();
		if (!this.codRed1.trim().isEmpty()) {
			this.lstConta = this.fncDAO.listarPlanoConta(Integer.parseInt(this.codRed1),
					Short.valueOf(String.valueOf(this.condominio)));
			if (this.lstConta.size() > 0) {
				this.cndplano = this.lstConta.get(0);
				this.rat.setNomeContaReduzida1(this.cndplano.getNome());
				this.codRed1 = String.valueOf(this.cndplano.getCod_reduzido());
				this.rat.setContaGradico1(this.cndplano.getCodigo_grafico());
			} else {
				this.msgContaN();
				this.rat.setNomeContaReduzida1("");
			}
		} else {
			this.rat.setNomeContaReduzida1("");
		}
	}

	public void pesquisaContaRatCod2() {
		this.fncDAO = new FinanceiroDAO();
		if (!this.codRed2.trim().isEmpty()) {
			this.lstConta = this.fncDAO.listarPlanoConta(Integer.parseInt(this.codRed2),
					Short.valueOf(String.valueOf(this.condominio)));
			if (this.lstConta.size() > 0) {
				this.cndplano = this.lstConta.get(0);
				this.rat.setNomeContaReduzida2(this.cndplano.getNome());
				this.codRed2 = String.valueOf(this.cndplano.getCod_reduzido());
				this.rat.setContaGradico2(this.cndplano.getCodigo_grafico());
			} else {
				this.msgContaN();
				this.rat.setNomeContaReduzida2("");
			}
		} else {
			this.rat.setNomeContaReduzida2("");
		}
	}

	public void pesquisaContaRatCod3() {
		this.fncDAO = new FinanceiroDAO();
		if (!this.codRed3.trim().isEmpty()) {
			this.lstConta = this.fncDAO.listarPlanoConta(Integer.parseInt(this.codRed3),
					Short.valueOf(String.valueOf(this.condominio)));
			if (this.lstConta.size() > 0) {
				this.cndplano = this.lstConta.get(0);
				this.rat.setNomeContaReduzida3(this.cndplano.getNome());
				this.codRed3 = String.valueOf(this.cndplano.getCod_reduzido());
				this.rat.setContaGradico3(this.cndplano.getCodigo_grafico());
			} else {
				this.msgContaN();
				this.rat.setNomeContaReduzida3("");
			}
		} else {
			this.rat.setNomeContaReduzida3("");
		}
	}

	public void pesquisaContaRatCod4() {
		this.fncDAO = new FinanceiroDAO();
		if (!this.codRed4.trim().isEmpty()) {
			this.lstConta = this.fncDAO.listarPlanoConta(Integer.parseInt(this.codRed4),
					Short.valueOf(String.valueOf(this.condominio)));
			if (this.lstConta.size() > 0) {
				this.cndplano = this.lstConta.get(0);
				this.rat.setNomeContaReduzida4(this.cndplano.getNome());
				this.codRed4 = String.valueOf(this.cndplano.getCod_reduzido());
				this.rat.setContaGradico2(this.cndplano.getCodigo_grafico());
			} else {
				this.msgContaN();
				this.rat.setNomeContaReduzida4("");
			}
		} else {
			this.rat.setNomeContaReduzida4("");
		}
	}

	public void pesquisaContaRatCod5() {
		this.fncDAO = new FinanceiroDAO();
		if (!this.codRed5.trim().isEmpty()) {
			this.lstConta = this.fncDAO.listarPlanoConta(Integer.parseInt(this.codRed5),
					Short.valueOf(String.valueOf(this.condominio)));
			if (this.lstConta.size() > 0) {
				this.cndplano = this.lstConta.get(0);
				this.rat.setNomeContaReduzida5(this.cndplano.getNome());
				this.codRed5 = String.valueOf(this.cndplano.getCod_reduzido());
				this.rat.setContaGradico5(this.cndplano.getCodigo_grafico());
			} else {
				this.msgContaN();
				this.rat.setNomeContaReduzida5("");
			}
		} else {
			this.rat.setNomeContaReduzida5("");
		}
	}

	public void pesquisaContaRatCod6() {
		this.fncDAO = new FinanceiroDAO();
		if (!this.codRed6.trim().isEmpty()) {
			this.lstConta = this.fncDAO.listarPlanoConta(Integer.parseInt(this.codRed6),
					Short.valueOf(String.valueOf(this.condominio)));
			if (this.lstConta.size() > 0) {
				this.cndplano = this.lstConta.get(0);
				this.rat.setNomeContaReduzida6(this.cndplano.getNome());
				this.codRed6 = String.valueOf(this.cndplano.getCod_reduzido());
				this.rat.setContaGradico6(this.cndplano.getCodigo_grafico());
			} else {
				this.msgContaN();
				this.rat.setNomeContaReduzida6("");
			}
		} else {
			this.rat.setNomeContaReduzida6("");
		}
	}

	public void pesquisaContaCod() {
		this.fncDAO = new FinanceiroDAO();
		if (this.contaContabil != null) {
			this.lstConta = this.fncDAO.listarPlanoConta(Integer.parseInt(this.contaContabil),
					Short.valueOf(String.valueOf(this.condominio)));
			if (this.lstConta != null && this.lstConta.size() > 0) {
				this.cndplano = this.lstConta.get(0);
				if (this.cndplano != null) {
					this.nomeConta = this.cndplano.getNome();
					this.contaContabil = String.valueOf(this.cndplano.getCod_reduzido());
					if (this.contaContabil != null) {
						String h4 = String.valueOf(this.contaContabil.charAt(this.contaContabil.length() - 1));
						String h3 = String.valueOf(this.contaContabil.charAt(this.contaContabil.length() - 2));
						String h2 = String.valueOf(this.contaContabil.charAt(this.contaContabil.length() - 3));
						String h1 = String.valueOf(this.contaContabil.charAt(this.contaContabil.length() - 4));
						this.codigoHistPadrao = h1 + h2 + h3 + h4;
						this.listarHistPadrao();
					}
				}
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Conta não encontrada!", "Conta não encontrada!"));
				this.nomeConta = "";
			}
		} else {
			this.nomeConta = "";
		}
	}

	public void limparParcelado() {
		this.pci = "";
		this.pcf = "";
	}

	public void trataExibicaoHistorico() {
		if (this.rateado.equals("S")) {
			this.complemento = "";
			StringBuilder strBuilder = new StringBuilder();
			if (this.complemento1 != null && !this.complemento1.trim().isEmpty()) {
				strBuilder.append(constroiHistoricoRateado(this.complemento1));
			}
			if (this.complemento2 != null && !this.complemento2.trim().isEmpty()) {
				strBuilder.append("<br/>");
				strBuilder.append(constroiHistoricoRateado(this.complemento2));
			}
			if (this.complemento3 != null && !this.complemento3.trim().isEmpty()) {
				strBuilder.append("<br/>");
				strBuilder.append(constroiHistoricoRateado(this.complemento3));
			}
			if (this.complemento4 != null && !this.complemento4.trim().isEmpty()) {
				strBuilder.append("<br/>");
				strBuilder.append(constroiHistoricoRateado(this.complemento4));
			}
			if (this.complemento5 != null && !this.complemento5.trim().isEmpty()) {
				strBuilder.append("<br/>");
				strBuilder.append(constroiHistoricoRateado(this.complemento5));
			}
			if (this.complemento6 != null && !this.complemento6.trim().isEmpty()) {
				strBuilder.append("<br/>");
				strBuilder.append(constroiHistoricoRateado(this.complemento6));
			}
			this.historicoExibicaoRateado = strBuilder.toString();
		}
	}

	public String constroiHistoricoRateado(String complemento) {
		StringBuilder strBuilder = new StringBuilder();
		if (this.empresa != null && !this.empresa.isEmpty()) {
			strBuilder.append(this.empresa);
		}
		if (this.notaFiscal != null && !this.notaFiscal.isEmpty()) {
			if (this.empresa != null && !this.empresa.isEmpty()) {
				strBuilder.append(" - ");
			}
			strBuilder.append(this.tipoDocumento + " " + this.notaFiscal);
		}
		if (complemento != null && !complemento.isEmpty()) {
			if ((this.notaFiscal != null && !this.notaFiscal.isEmpty())
					|| (this.empresa != null && !this.empresa.isEmpty())) {
				strBuilder.append(" - ");
			}
			strBuilder.append(complemento);
		}
		return strBuilder.toString();
	}

	public void pesquisaFornecedor() {
		this.fncDAO = new FinanceiroDAO();
		if (this.fornecedor != null) {
			try {
				this.fornecedorSelecionado = this.fncDAO.listarFornecedor(Double.parseDouble(this.fornecedor));
				if (this.fornecedorSelecionado != null && !this.fornecedorSelecionado.getNome().isEmpty()) {
					this.nomeFornecedor = this.fornecedorSelecionado.getNome();
				} else {
					this.nomeFornecedor = "";
					this.fornecedor = "";
					this.fornecedorSelecionado = null;
				}
			} catch (NumberFormatException e) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Este campo só aceita números", "ste campo só aceita números"));
			}
		}
	}

	public void limpaTipoConta() {
		this.codBanco = "";
		this.codAgencia = "";
		this.nomeDoBanco = "";
		this.nomeFavorecido = "";
		this.contaPoupanca = null;
		this.tipoPessoa = null;
		this.cc = "";
		this.dac = "";
		this.tipoPessoa = "";
		this.cpf_cnpj = "";
		this.tipoPagto = "";
		this.codMovimento = "";
		this.codCompensacao = "";
		this.codigoBarras = "";
		this.ldCampo1 = "";
		this.ldCampo2 = "";
		this.ldCampo3 = "";
		this.ldDac = "";
		this.ldValor = "";
		this.concSegbarra1 = "";
		this.concSegbarra2 = "";
		this.concSegbarra3 = "";
		this.concSegbarra4 = "";
		this.codBarras = "";
		this.checkCPF = 0;
		this.usarTED = false;
	}

	public void pesquisarBanco() {
		if (this.codBanco != null) {
			this.fncDAO = new FinanceiroDAO();
			int valor = Integer.valueOf(this.codBanco);
			this.bancoSelecionado = this.fncDAO.pesquisarBanco(valor);
			if (this.bancoSelecionado == null) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Banco não encontrado!", "Banco não encontrado!"));
				this.nomeDoBanco = "";
			} else {
				this.nomeDoBanco = this.bancoSelecionado.getNomeDoBanco();
			}
		} else {
			this.nomeDoBanco = "";
		}
	}

	public void pesquisaConta() {
		if (this.nomeConta == null || this.nomeConta.isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Insira o nome da conta para pesquisar!", "Insira o nome da conta para pesquisar!"));
		} else {
			this.fncDAO = new FinanceiroDAO();
			if (this.hideSalvar == 1 || this.hideSalvar == 2) {
				this.condominio = this.cndpagar.getCondominio();
			}
			this.lstConta = this.fncDAO.listarPlanoContaNome(this.nomeConta, this.condominio);
			if (this.lstConta.size() == 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Nenhum resultado encontrado!", "Nenhum resultado encontrado!"));
			} else {
				RequestContext.getCurrentInstance().execute("PF('dlgResultadoConta').show();");
			}
		}
	}

	public void selecionarContaContabil() {
		this.contaContabil = String.valueOf(this.cndplano.getCod_reduzido());
		this.nomeConta = this.cndplano.getNome();
		this.nomeCapa = this.cndplano.getNome_capa();

		RequestContext.getCurrentInstance().execute("PF('dlgResultadoConta').hide();");
		RequestContext.getCurrentInstance().execute("PF('dlgPesqConta').hide();");
	}

	public void selecionarContaContabil2() {
		this.contaContabil = String.valueOf(this.cndplano.getCod_reduzido());
		this.nomeConta = this.cndplano.getNome();
		this.nomeCapa = this.cndplano.getNome_capa();

		RequestContext.getCurrentInstance().execute("PF('dlgResultadoConta2').hide();");
		RequestContext.getCurrentInstance().execute("PF('dlgPesqConta2').hide();");
	}

	public void listarFornecedoresCNPJ() {
		this.fncDAO = new FinanceiroDAO();
		if (this.fornecedor != null && !this.fornecedor.equals("")) {
			this.lstFornecedor = this.fncDAO.listarFornecedoresCNPJ(this.fornecedor);
			if (this.lstFornecedor.size() == 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Nenhum resultado encontrado!", "Nenhum resultado encontrado!"));
			} else {
				RequestContext.getCurrentInstance().execute("PF('dlgResultadoFornecedor').show();");
			}
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Insira um CPF/CNPJ para pesquisar!", "Insira um CPF/CNPJ para pesquisar!"));
		}
	}

	public void selecionarFornecedor() {
		if (this.fornecedorSelecionado != null) {
			this.fornecedor = String.valueOf(this.fornecedorSelecionado.getInscricao());
			this.nomeFornecedor = this.fornecedorSelecionado.getNome();
			RequestContext.getCurrentInstance().execute("PF('dlgPesqFornecedor').hide();");
			RequestContext.getCurrentInstance().execute("PF('dlgResultadoFornecedor').hide();");
		}
	}

	public void pesquisaImagem() {
		this.validaEtiqueta = false;
		if (this.idImagem != null && !this.idImagem.isEmpty() && Double.parseDouble(this.idImagem) > 0) {
			this.fncDAO = new FinanceiroDAO();

			BigDecimal bd = new BigDecimal(this.idImagem);

			for (BigDecimal b : this.fncDAO.validaIdImg()) {
				if (bd.equals(b)) {
					this.validaEtiqueta = true;
				}
			}

			if (this.validaEtiqueta) {

				this.imagemSelecionada = this.fncDAO.pesquisaImagem(Double.parseDouble(this.idImagem),
						this.getCondominio());

				if (this.imagemSelecionada.getId() == 0.0) {
					this.imagemSelecionada = this.fncDAO.pesquisaImagem(Double.parseDouble(this.idImagem),
							this.cndpagar.getCondominio());
				}

				if (this.imagemSelecionada.getCodigo() > 0) {
					this.nomeArquivo = this.imagemSelecionada.getNome_arquivo();
					this.arquivo = this.imagemSelecionada.getImagem();
					RequestContext.getCurrentInstance().update("frmUploadArquivo");
				} else {
					this.nomeArquivo = null;
					this.arquivo = null;
				}
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"O número da etiqueta não é válido!", "O número da etiqueta não é válido!"));
			}

		} else {
			this.nomeArquivo = null;
		}
	}

	public void adicionarArquivo() {
		this.fncDAO = new FinanceiroDAO();
		if (this.validaEtiqueta) {
			if (this.listaArquivos == null) {
				this.listaArquivos = new ArrayList<>();
			}
			if (this.idImagem == null) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Insira um id para o arquivo!", "Insira um id para o arquivo!"));
				UIComponent component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmUploadArquivo:txtIdArquivo");
				if (component != null) {
					((UIInput) component).setValid(false);
				}
			} else if (this.arquivo == null) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Insira um arquivo para adicionar!", "Insira um arquivo para adicionar!"));
			} else {
				try {
					for (financeiro_imagem aux : this.listaArquivos) {
						if (Double.valueOf(this.idImagem) == aux.getId()) {
							throw new RNException("Este id já está associado a um arquivo deste lançamento!");
						}
					}
					if (this.imagemSelecionada == null || this.imagemSelecionada.getCodigo() == 0
							|| this.imagemSelecionada.getId() != Double.parseDouble(this.idImagem)) {
						financeiro_imagem img = new financeiro_imagem();
						img.setNome_arquivo(this.nomeArquivo);
						img.setImagem(this.arquivo);
						img.setId(Double.parseDouble(this.idImagem));
						img.setDataVinculoArq(new Date());
						img.setIdentificacaoTipoArq(".PDF");
						img.setCdCnd(Short.valueOf(String.valueOf(this.condominio)));
						this.listaArquivos.add(img);
					} else {
						this.listaArquivos.add(this.imagemSelecionada);
					}
					this.arquivo = null;
					this.nomeArquivo = null;
					this.idImagem = null;
					RequestContext.getCurrentInstance().execute("PF('dlgUploadArquivo').hide();");
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (RNException e) {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage(), ""));
					UIComponent component = FacesContext.getCurrentInstance().getViewRoot()
							.findComponent("frmUploadArquivo:txtIdArquivo");
					((UIInput) component).setValid(false);
				}
			}
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Insira um número de etiqueta válida!", "Insira um número de etiqueta válida!"));
		}
	}

	public void excDocumento(int index) {
		this.listaArquivos.remove(index);
	}

	public void dwldDocumento(int index) throws FileNotFoundException {
		ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext()
				.getContext();
		String arquivo = servletContext.getRealPath("/") + File.separator + "resources" + File.separator + "arquivos"
				+ File.separator + this.listaArquivos.get(index).getNome_arquivo();
		FileOutputStream documento = null;
		try {
			documento = new FileOutputStream(new File(arquivo));
			documento.write(this.listaArquivos.get(index).getImagem(), 0,
					this.listaArquivos.get(index).getImagem().length);
			this.cdImagem = (int) this.listaArquivos.get(index).getId();
			documento.flush();
			documento.close();
		} catch (Exception e) {
			throw new FacesException("Error in writing captured file.");
		}
		InputStream stream = new FileInputStream(arquivo);
		this.arquivoDownload = new DefaultStreamedContent(stream, "", this.listaArquivos.get(index).getNome_arquivo());
	}

	public void visualizarImagem(int index) throws FileNotFoundException {
		this.cdImagem = (int) this.listaArquivos.get(index).getId();
		this.imagemSelecionada = this.listaArquivos.get(index);
	}

	public void validaGrid2() {
		UIComponent component = null;
		boolean sucesso = false;
		switch (this.tipoPagamento) {
		case "5":
			sucesso = true;
			if (this.nomeFavorecido == null || this.nomeFavorecido.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:txtNomeFavorecidoCC");
				((UIInput) component).setValid(false);

			}
			if (this.codBanco == null || this.nomeFavorecido.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:txtCodBancoCC");
				((UIInput) component).setValid(false);

			}
			if (this.codAgencia == null || this.codAgencia.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:txtCodAgenciaCC");
				((UIInput) component).setValid(false);

			}
			if (this.cc == null || this.cc.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot().findComponent("frmLancamento:txtContaCC");
				((UIInput) component).setValid(false);

			}
			if (this.dac == null || this.dac.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot().findComponent("frmLancamento:txtDigitoCC");
				((UIInput) component).setValid(false);

			}
			if (this.contaPoupanca == null || this.contaPoupanca.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:oneRadioCPoupancaCC");
				((UIInput) component).setValid(false);

			}
			if (this.tipoPessoa == null || this.tipoPessoa.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:oneRadioTipoPessoaCC");
				((UIInput) component).setValid(false);

			}
			if (this.cpf_cnpj == null || this.cpf_cnpj.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot().findComponent("frmLancamento:txtCpfCnpjCC");
				((UIInput) component).setValid(false);

			}

			;
			break;
		case "7":
			sucesso = true;
			if (this.nomeFavorecido == null || this.nomeFavorecido.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:txtNomeFavorecidoDoc");
				((UIInput) component).setValid(false);

			}
			if (this.codBanco == null || this.nomeFavorecido.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:txtCodBancoDoc");
				((UIInput) component).setValid(false);

			}
			if (this.codAgencia == null || this.codAgencia.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:txtCodAgenciaDoc");
				((UIInput) component).setValid(false);

			}
			if (this.cc == null || this.cc.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot().findComponent("frmLancamento:txtContaDoc");
				((UIInput) component).setValid(false);

			}
			if (this.dac == null || this.dac.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot().findComponent("frmLancamento:txtDigitoDoc");
				((UIInput) component).setValid(false);

			}
			if (this.contaPoupanca == null || this.contaPoupanca.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:txtCPoupancaDoc");
				((UIInput) component).setValid(false);

			}
			if (this.tipoPessoa == null || this.tipoPessoa.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:txtTipoPessoaDoc");
				((UIInput) component).setValid(false);

			}
			if (this.cpf_cnpj == null || this.cpf_cnpj.trim().isEmpty()) {
				sucesso = false;
				component = FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("frmLancamento:txtCpfCnpjDoc");
				((UIInput) component).setValid(false);

			}
			;
			break;
		case "8":
			if (this.codigoBarras != null && !this.codigoBarras.trim().isEmpty()) {
				sucesso = this.listarLinhaDigitavel();
			} else {
				sucesso = listarCodigoBarras();
			}
			;
			break;
		case "E":
			if (this.codigoBarras != null && !this.codigoBarras.trim().isEmpty()) {
				sucesso = this.listarLinhaDigitavel();
			} else {
				sucesso = listarCodigoBarras();
			}
			break;
		default:
			break;
		}
		if (this.condominio < 110) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Selecione o Condomínio!", "Selecione o Condomínio!"));
			UIComponent component2 = FacesContext.getCurrentInstance().getViewRoot()
					.findComponent("frmCondominio:console");
			((UIInput) component2).setValid(false);
			this.grid3 = false;
			sucesso = false;
		}
		if (sucesso) {
			this.grid2 = false;
			this.grid3 = true;
		}
	}

	public boolean listarCodigoBarras() {
		String codigoBarras = null;
		boolean sucesso = true;
		if (this.tipoPagamento.equals("E")) {
			if (this.validaLDConcessionaria()) {
				String parte1 = this.concSegbarra1.substring(0, this.concSegbarra1.length() - 1);
				String parte2 = this.concSegbarra2.substring(0, this.concSegbarra2.length() - 1);
				String parte3 = this.concSegbarra3.substring(0, this.concSegbarra3.length() - 1);
				String parte4 = this.concSegbarra4.substring(0, this.concSegbarra4.length() - 1);
				codigoBarras = parte1 + parte2 + parte3 + parte4;
				this.codigoBarras = codigoBarras;
			} else {
				sucesso = false;
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Insira uma linha digitável válida!", "Insira uma linha digitável válida!"));
				this.codigoBarras = null;
			}
		} else if (this.tipoPagamento.equals("8")) {
			if (this.validaLDBoleto()) {
				String linhaDigitavel = this.ldCampo1 + this.ldCampo2 + this.ldCampo3 + this.ldDac + this.ldValor;
				linhaDigitavel = linhaDigitavel.replace(".", "");
				codigoBarras = linhaDigitavel.substring(0, 4) + linhaDigitavel.substring(32, 47)
						+ linhaDigitavel.substring(4, 9) + linhaDigitavel.substring(10, 20)
						+ linhaDigitavel.substring(21, 31);
				this.codigoBarras = codigoBarras;
			} else {
				sucesso = false;
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Insira uma linha digitável válida!", "Insira uma linha digitável válida!"));
				this.codigoBarras = null;
			}
		}
		return sucesso;
	}

	public boolean validaLDConcessionaria() {
		boolean valido = true;
		if (this.concSegbarra1 == null || this.concSegbarra1.length() != 12 || this.concSegbarra1.contains("_")) {
			valido = false;
		}
		if (this.concSegbarra2 == null || this.concSegbarra2.length() != 12 || this.concSegbarra2.contains("_")) {
			valido = false;
		}
		if (this.concSegbarra3 == null || this.concSegbarra3.length() != 12 || this.concSegbarra3.contains("_")) {
			valido = false;
		}
		if (this.concSegbarra4 == null || this.concSegbarra4.length() != 12 || this.concSegbarra4.contains("_")) {
			valido = false;
		}
		return valido;
	}

	public boolean validaLDBoleto() {
		boolean valido = true;
		if (this.ldCampo1 == null || this.ldCampo1.length() != 11 || this.ldCampo1.contains("_")) {
			valido = false;
		}
		if (this.ldCampo2 == null || this.ldCampo2.length() != 12 || this.ldCampo2.contains("_")) {
			valido = false;
		}
		if (this.ldCampo3 == null || this.ldCampo3.length() != 12 || this.ldCampo3.contains("_")) {
			valido = false;
		}
		if (this.ldDac == null || this.ldDac.length() != 1 || this.ldDac.contains("_")) {
			valido = false;
		}
		if (this.ldValor == null || this.ldValor.length() != 14 || this.ldValor.contains("_")) {
			valido = false;
		}
		return valido;
	}

	public void upDocs(FileUploadEvent event) throws IOException {
		this.arquivo = null;
		this.nomeArquivo = "";
		InputStream is = event.getFile().getInputstream();
		int tamanho = is.available();
		if (tamanho > 6000000) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"O tamanho do arquivo é muito grande!", "O tamanho do arquivo é muito grande!"));
		} else {
			byte[] buf = null;
			int len;
			int size = is.available();
			if (is instanceof ByteArrayInputStream) {
				size = is.available();
				buf = new byte[size];
				len = is.read(buf, 0, size);
			} else {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				buf = new byte[size];
				while ((len = is.read(buf, 0, size)) != -1)
					bos.write(buf, 0, len);
				buf = bos.toByteArray();
			}
			this.arquivo = buf;
			this.nomeArquivo = event.getFile().getFileName();
			event = null;
		}
	}

	public void pesquisaFavorecido() {
		this.fncDAO = new FinanceiroDAO();

		this.lstFavorecido = this.fncDAO.listarFavorecido(this.nomeFavorecido, this.codigoFavorecido);

		if (this.lstFavorecido != null && this.lstFavorecido.size() > 0) {
			RequestContext.getCurrentInstance().execute("PF('dlgResultadoFavorecido').show();");
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Nenhum resultado encontrado!", "Nenhum resultado encontrado!"));
		}
	}

	public void selecionarFavorecido() {
		if (this.favorecidoSelecionado != null) {

			this.cndpagar.setCodigoFav(this.favorecidoSelecionado.getCodigo());

			this.nomeFavorecido = this.favorecidoSelecionado.getFavorecido();
			this.codBanco = String.valueOf(this.favorecidoSelecionado.getBanco());
			this.codAgencia = String.valueOf(this.favorecidoSelecionado.getAgencia());
			this.nomeDoBanco = this.favorecidoSelecionado.getNomeBanco();
			this.cc = this.favorecidoSelecionado.getContaCorrente();
			this.dac = this.favorecidoSelecionado.getDacConta();

			this.contaPoupanca = this.favorecidoSelecionado.getContaPoupanca();

			this.tipoPessoa = this.favorecidoSelecionado.getTipoPessoa();
			this.cpf_cnpj = String.valueOf(BigInteger.valueOf(this.favorecidoSelecionado.getCnpjCpf()));
		}
		RequestContext.getCurrentInstance().execute("PF('dlgResultadoFavorecido').hide();");
		RequestContext.getCurrentInstance().execute("PF('dlgPesqFavorecido').hide();");
	}

	public void listarBancosPorNome() {
		if (this.nomeDoBanco != null) {
			this.fncDAO = new FinanceiroDAO();
			this.lstBancos = this.fncDAO.listarBancosPorNome(this.nomeDoBanco);
			if (this.lstBancos == null || this.lstBancos.size() == 0) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Banco não encontrado!", "Banco não encontrado!"));
			} else {
				RequestContext.getCurrentInstance().execute("PF('dlgResultadoBanco').show();");
			}
		}
	}

	public void selecionarBanco() {
		if (this.bancoSelecionado != null) {
			this.nomeDoBanco = this.bancoSelecionado.getNomeDoBanco();
			this.codBanco = String.valueOf(this.bancoSelecionado.getCodBanco());
			RequestContext.getCurrentInstance().execute("PF('dlgPesqBanco').hide();");
			RequestContext.getCurrentInstance().execute("PF('dlgResultadoBanco').hide();");
		}
	}

	public void validaLancamento() throws ParseException {
		this.fncDAO = new FinanceiroDAO();
		this.constroiCndpagar();
		cndpagar c1 = new cndpagar();
		if (this.validaCPF()) {
			if (!this.cndpagar.getCodigoBarra().trim().isEmpty()) {
				c1 = this.fncDAO.pesqDupliVCCB(this.cndpagar);
				if (c1 != null) {
					if (c1.getCodigo() != 0) {
						this.valCodLacto = c1.getCodigo();
						this.valDatVenc = c1.getVencimento();
						if (!c1.getCodigoBarra().trim().isEmpty()) {
							this.valCodBarras = c1.getCodigoBarra();
						}
						this.valValida = true;
						RequestContext.getCurrentInstance().update("frmValidalancamento");
						RequestContext.getCurrentInstance().execute("PF('dlgValLancto').show();");
					}
				} else {
					this.adicionarLancamentoOma();
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage("Adicionado com sucesso!", "Adicionado com sucesso!"));
				}
			} else {
				this.valor = this.valor.replace("R$ ", "");
				DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));
				double valor2 = df.parse(this.valor).doubleValue();
				c1 = this.fncDAO.pesqDupliVCAGCCVL(this.cndpagar, valor2);
				if (c1 != null) {
					if (c1.getCodigo() != 0) {
						this.valCodLacto = c1.getCodigo();
						this.valDatVenc = c1.getVencimento();
						this.valValor = c1.getValor();
						this.valCodAg = String.valueOf(c1.getAgencDestino());
						this.valCC = c1.getContaDestino();
						this.valValida = false;
						RequestContext.getCurrentInstance().update("frmValidalancamento");
						RequestContext.getCurrentInstance().execute("PF('dlgValLancto').show();");
					}
				} else {
					this.adicionarLancamentoOma();
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage("Adicionado com sucesso!", "Adicionado com sucesso!"));
				}
			}
		}
	}

	public void constroiCndpagar() {
		this.cndpagar.setUsuarioAprovacao(null);
		this.fncDAO = new FinanceiroDAO();
		if (this.cndpagar.getCodigo() == 0 && !this.alteracao) {
			this.cndpagar.setCondominio(Short.valueOf(String.valueOf(this.condominio)));
			this.cndpagar.setAdicionadoPor(this.sessaoMB.getUsuario().getEmail());
			this.cndpagar.setDataInclusao(new Date());
		}
		this.condoParam = this.fncDAO.condominioParam(Short.valueOf(cndpagar.getCondominio()));
		if (this.condoParam != null) {
			if (this.condoParam.isAprovacaoLancamentos()) {
				this.cndpagar.setStatus("Z");
			} else {
				this.cndpagar.setStatus("P");
			}
		} else {
			this.cndpagar.setStatus("P");
		}

		if (this.classificacao != null) {
			this.cndpagar.setClassificacao(this.classificacao);
		} else {
			this.cndpagar.setClassificacao(null);
		}

		if (this.vencimento != null) {
			this.cndpagar.setVencimento(this.vencimento);
		} else {
			this.cndpagar.setVencimento(null);
		}

		if (this.valor != null && !this.valor.trim().isEmpty()) {
			this.valor = this.valor.replace("R$ ", "");
			DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));
			try {
				this.cndpagar.setValor(df.parse(this.valor).doubleValue());
				this.cndpagar.setValorLancto(df.parse(this.valor).doubleValue());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			this.cndpagar.setValor(0.0);
		}

		if (this.valorGed > 0) {
			this.cndpagar.setValorLancto(this.valorGed);
		}

		if (this.contaContabil != null && !this.contaContabil.trim().isEmpty()) {
			this.cndpagar.setConta(Integer.parseInt(this.contaContabil));
			this.cndpagar.setCtaAnlFinanc(Integer.valueOf(this.cndplano.getCodigo_grafico()));
		} else {
			this.cndpagar.setConta(0);
			this.cndpagar.setCtaAnlFinanc(0);
		}

		if (this.tipoDocumento != null) {
			this.cndpagar.setTipoDocumento(this.tipoDocumento);
		} else {
			this.cndpagar.setTipoDocumento(null);
		}

		if (this.fornecedorSelecionado != null) {
			this.cndpagar.setCredor(String.valueOf(this.fornecedorSelecionado.getUsualcred()));
		}

		if (this.notaFiscal != null && !this.notaFiscal.trim().isEmpty()) {
			this.cndpagar.setNumeroNf(BigInteger.valueOf(Long.valueOf(this.notaFiscal)));
			this.cndpagar.setNf(this.notaFiscal);
		} else {
			this.cndpagar.setNotaFiscal(0);
		}
		if (this.bloco != null && !this.bloco.isEmpty()) {
			this.cndpagar.setBloco(this.bloco);
		} else {
			this.cndpagar.setBloco("0");
		}

		if (this.dtEmissaoNF != null) {
			this.cndpagar.setEmissaoNf(this.dtEmissaoNF);
		} else {
			this.cndpagar.setEmissaoNf(null);
		}

		StringBuilder strBuilder = new StringBuilder();
		String historico = null;
		if (this.empresa != null && !this.empresa.isEmpty()) {
			strBuilder.append(this.empresa);
			this.cndpagar.setEmpresa(this.empresa);
		} else {
			this.cndpagar.setEmpresa(null);
		}

		if (this.notaFiscal != null && !this.notaFiscal.isEmpty()) {
			if (this.empresa != null && !this.empresa.isEmpty()) {
				strBuilder.append(" - ");
			}
			if (!this.notaFiscal.trim().isEmpty()) {
				strBuilder.append(this.tipoDocumento + " " + this.notaFiscal);
			}
		}

		if (this.complemento != null && !this.complemento.isEmpty()) {
			if ((this.notaFiscal != null && !this.notaFiscal.isEmpty())
					|| (this.empresa != null && !this.empresa.isEmpty())) {
				strBuilder.append(" - ");
			}
			strBuilder.append(this.complemento);
			this.cndpagar.setHist(this.complemento);
		} else {
			this.cndpagar.setHist(null);
		}

		if (this.parcelamento != null) {
			if (this.parcelamento.equals("S")) {
				this.cndpagar.setPcInicial(Integer.valueOf(this.pci));
				this.cndpagar.setPcFinal(Integer.valueOf(this.pcf));
				strBuilder.append(" - ");
				strBuilder.append("Parc. " + this.pci + "/" + this.pcf);
				historico = strBuilder.toString();
				this.cndpagar.setHistorico(historico);
			} else {
				this.pci = "";
				this.pcf = "";
				this.cndpagar.setPcInicial(0);
				this.cndpagar.setPcFinal(0);
			}
		}

		historico = strBuilder.toString();
		this.cndpagar.setHistorico(historico);

		if (this.hideSalvar == 1 || this.hideSalvar == 2) {
			if (this.codigoHistPadrao != null) {
				if (!this.codigoHistPadrao.trim().isEmpty()) {
					this.cndpagar.setHistorico(this.complemento);
				} else {
					this.cndpagar.setHistorico(this.complemento);
				}
			} else {
				this.cndpagar.setHistorico(historico);
			}
		}

		if (this.obsLancto != null && !this.obsLancto.isEmpty()) {
			this.cndpagar.setObs("S");
		} else {
			this.cndpagar.setObs("N");
		}

		// ---------------------------------------------------//

		// SEGUNDA TELA TIPO DE PAGAMENTO
		if (this.codBarras != null) {
			this.cndpagar.setCodigoBarra(this.codigoBarras);
		}

		if (this.tipoPagamento != null) {
			this.fncDAO = new FinanceiroDAO();

			if (this.tipoPagamento.equals("5")) {
				this.cndpagar.setFavorecido(this.nomeFavorecido);
				this.cndpagar.setAgencDestino(Integer.valueOf(this.codAgencia));
				this.cndpagar.setBancoDestino(Short.valueOf(this.codBanco));
				this.cndpagar.setContaDestino(this.cc);
				this.cndpagar.setDigAgeDest(this.dac);
				this.cndpagar.setTipoPessoa(this.tipoPessoa);
				this.cndpagar.setCnpj(Double.valueOf(this.cpf_cnpj));
				this.cndpagar.setTipoPagto(this.tipoPagamento);
				this.cndpagar.setTipoContaBancaria(this.contaPoupanca);

				if (this.condominio == 4241) {
					this.cndpagar.setModalPagto("20");
				} else {
					this.cndpagar.setModalPagto("0");
					this.cndpagar.setTipoConta("01");
				}

				if (this.contaPoupanca.equals("S")) {
					this.cndpagar.setPgCredpoup("S");
				} else {
					this.cndpagar.setPgCredpoup("N");
				}

				if (this.parcelamento != null) {
					if (this.parcelamento.equals("S")) {
						this.cndpagar.setParcelado(this.parcelamento);
						this.cndpagar.setPcInicial(Integer.valueOf(this.pci));
						this.cndpagar.setPcFinal(Integer.valueOf(this.pcf));
					} else {
						this.cndpagar.setParcelado("N");
					}
				} else {
					this.cndpagar.setParcelado("N");
				}
				if (this.hideSalvar == 1 || this.hideSalvar == 2) {
					short codConta = this.fncDAO
							.listContCondo(Short.valueOf(String.valueOf(this.cndpagar.getCondominio())));
					this.cndpagar.setContaBancaria(codConta);
				} else {
					short codConta = this.fncDAO.listContCondo(Short.valueOf(String.valueOf(this.condominio)));
					this.cndpagar.setContaBancaria(codConta);
				}

				this.cndpagar.setEstimado("R");
				if (this.favorecidoSelecionado != null && this.favorecidoSelecionado.getCodigo() != 0) {
					this.cndpagar.setCodigoFav(this.favorecidoSelecionado.getCodigo());
				} else {
					System.out.println("erro favorecido");
				}

			} else if (this.tipoPagamento.equals("7")) {

				this.cndpagar.setFavorecido(this.nomeFavorecido);
				this.cndpagar.setAgencDestino(Integer.valueOf(this.codAgencia));
				this.cndpagar.setBancoDestino(Short.valueOf(this.codBanco));

				if (this.cndpagar.getValorLancto() > 250.00) {
					this.cndpagar.setTipoDoDoc("S");
				}

				if (this.contaPoupanca != null) {
					if (this.contaPoupanca.equals("S")) {
						this.cndpagar.setModalPagto((this.condominio == 4241 ? "20" : "11"));
						this.cndpagar.setTipoConta("02");
						this.cndpagar.setTipoContaBancaria(this.contaPoupanca);
						this.cndpagar.setPgCredpoup("S");
					} else {
						this.cndpagar.setModalPagto((this.condominio == 4241 ? "20" : "01"));
						this.cndpagar.setTipoConta("01");
						this.cndpagar.setTipoContaBancaria(this.contaPoupanca);
						this.cndpagar.setPgCredpoup("N");
					}
				} else {
					this.cndpagar.setModalPagto(null);
					this.cndpagar.setTipoConta(null);
					this.cndpagar.setTipoContaBancaria(null);
				}

				if (this.parcelamento != null) {
					if (this.parcelamento.equals("S")) {
						this.cndpagar.setParcelado(this.parcelamento);
						this.cndpagar.setPcInicial(Integer.valueOf(this.pci));
						this.cndpagar.setPcFinal(Integer.valueOf(this.pcf));
					} else {
						this.cndpagar.setParcelado("N");
					}
				} else {
					this.cndpagar.setParcelado("N");
				}

				if (this.hideSalvar == 1 || this.hideSalvar == 2) {
					short codConta = this.fncDAO
							.listContCondo(Short.valueOf(String.valueOf(this.cndpagar.getCondominio())));
					this.cndpagar.setContaBancaria(codConta);
				} else {
					short codConta = this.fncDAO.listContCondo(Short.valueOf(String.valueOf(this.condominio)));
					this.cndpagar.setContaBancaria(codConta);
				}

				this.cndpagar.setContaDestino(this.cc);
				this.cndpagar.setDigAgeDest(this.dac);
				this.cndpagar.setTipoPessoa(this.tipoPessoa);
				this.cndpagar.setCnpj(Double.valueOf(this.cpf_cnpj));
				this.cndpagar.setTipoPagto(this.tipoPagamento);
				this.cndpagar.setEstimado("R");
				if (this.favorecidoSelecionado != null && this.favorecidoSelecionado.getCodigo() != 0) {
					this.cndpagar.setCodigoFav(this.favorecidoSelecionado.getCodigo());
				} else {
					System.out.println("erro favorecido");
				}

			} else if (this.tipoPagamento.equals("8")) {
				short cod = Short.valueOf(String.valueOf(this.codigoBarras.subSequence(0, 3)));

				if (this.hideSalvar == 1 || this.hideSalvar == 2) {
					short codConta = this.fncDAO
							.listContCondo(Short.valueOf(String.valueOf(this.cndpagar.getCondominio())));
					this.cndpagar.setContaBancaria(codConta);
				} else {
					short codConta = this.fncDAO.listContCondo(Short.valueOf(String.valueOf(this.condominio)));
					this.cndpagar.setContaBancaria(codConta);
				}

				atbancos c = new atbancos();
				c = this.fncDAO.listBancoOMA2(cod);

				this.cndpagar.setBancoDestino(c.getCodBanco());
				this.cndpagar.setFavorecido(c.getNomeDoBanco());

				this.cndpagar.setLdCampo1(Double.valueOf(this.ldCampo1));
				this.cndpagar.setLdCampo2(Double.valueOf(this.ldCampo2));
				this.cndpagar.setLdCampo3(Double.valueOf(this.ldCampo3));
				Double valor = Double.valueOf(this.ldValor);
				this.cndpagar.setLdValor((valor == 0.0 ? 0 : valor));
				this.cndpagar.setLdDac(Integer.valueOf(this.ldDac));

				this.cndpagar.setEstimado("R");
				this.cndpagar.setTipoPagto(this.tipoPagamento);
				this.cndpagar.setTipoPessoa("J");

				this.cndpagar.setCodigoFav(cod);

				if (this.parcelamento != null) {
					if (this.parcelamento.equals("S")) {
						this.cndpagar.setParcelado(this.parcelamento);
						this.cndpagar.setPcInicial(Integer.valueOf(this.pci));
						this.cndpagar.setPcFinal(Integer.valueOf(this.pcf));
					} else {
						this.cndpagar.setParcelado("N");
					}
				} else {
					this.cndpagar.setParcelado("N");
				}

			} else if (this.tipoPagamento.equals("E")) {
				short codConta = 0;
				if (this.hideSalvar == 1 || this.hideSalvar == 2) {
					codConta = this.fncDAO.listContCondo(Short.valueOf(String.valueOf(this.cndpagar.getCondominio())));
					this.cndpagar.setContaBancaria(codConta);
				} else {
					codConta = this.fncDAO.listContCondo(Short.valueOf(String.valueOf(this.condominio)));
					this.cndpagar.setContaBancaria(codConta);
				}

				atbancos c = new atbancos();
				c = this.fncDAO.listBancoOMA(codConta);

				this.cndpagar.setContaBancaria(codConta);
				this.cndpagar.setBancoDestino((short) 0);
				this.cndpagar.setCodigoFav(Integer.valueOf(c.getCodBanco()));
				this.cndpagar.setFavorecido(c.getNomeDoBanco());

				this.cndpagar.setConcSegbarra1(Double.valueOf(this.concSegbarra1));
				this.cndpagar.setConcSegbarra2(Double.valueOf(this.concSegbarra2));
				this.cndpagar.setConcSegbarra3(Double.valueOf(this.concSegbarra3));
				this.cndpagar.setConcSegbarra4(Double.valueOf(this.concSegbarra4));
				this.cndpagar.setTipoPagto(this.tipoPagamento);
				this.cndpagar.setTipoPessoa("J");
				this.cndpagar.setEstimado("R");

				if (this.parcelamento != null) {
					if (this.parcelamento.equals("S")) {
						this.cndpagar.setParcelado(this.parcelamento);
						this.cndpagar.setPcInicial(Integer.valueOf(this.pci));
						this.cndpagar.setPcFinal(Integer.valueOf(this.pcf));
					} else {
						this.cndpagar.setParcelado("N");
					}
				} else {
					this.cndpagar.setParcelado("N");
				}
			}
		}

		try {
			this.salvarDetalhamento();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void adicionarLancamentoOma() {
		this.cndpagar.setUsuarioAprovacao(null);
		this.fncDAO = new FinanceiroDAO();
		ReturnUltimoControlRatRN rur = new ReturnUltimoControlRatRN();

		this.cndpagar.setCodigoRateio(rur.getCount());

		boolean valida = this.fncDAO.adicionaLanctoOma(this.cndpagar, this.sessaoMB.getUsuario().getEmail(), "oma",
				this.listaArquivos, this.obsLancto);
		if (valida) {
			/* this.cndpagar = new cndpagar(); */
			this.obsLancto = "";
			this.grid3 = false;
			this.grid4 = true;

			RequestContext.getCurrentInstance().execute("PF('dlgObsLancto1').hide();");

			this.msgAdicinado();
		} else {
			this.msgErro();
		}
	}

	public String addLanctoOmaRateado() {
		this.cndpagar.setUsuarioAprovacao(null);
		String volta = null;
		this.fncDAO = new FinanceiroDAO();
		if (this.validaCPF()) {

			this.constroiCndpagar();
			/* this.constroiImagem(); */

			List<rateio> lista = new ArrayList<rateio>();

			for (int i = 1; i <= 6; i++) {
				rateio x = new rateio();
				for (rateio r : this.listaDeRateio) {
					if (i == 1 & r.getValor1() != 0) {
						x.setValor1(r.getValor1());
						x.setContaReduzida1(r.getContaReduzida1());
						x.setContaGradico1(r.getContaGradico1());
						x.setComplemento1(this.complemento1);
						x.setHistorico1(constroiHistoricoRateado(this.complemento1));
					} else if (i == 2 & r.getValor2() != 0) {
						x.setValor2(r.getValor2());
						x.setContaReduzida2(r.getContaReduzida2());
						x.setContaGradico2(r.getContaGradico2());
						x.setComplemento2(this.complemento2);
						x.setHistorico2(constroiHistoricoRateado(this.complemento2));
					} else if (i == 3 & r.getValor3() != 0) {
						x.setValor3(r.getValor3());
						x.setContaReduzida3(r.getContaReduzida3());
						x.setContaGradico3(r.getContaGradico3());
						x.setComplemento3(this.complemento3);
						x.setHistorico3(constroiHistoricoRateado(this.complemento3));
					} else if (i == 4 & r.getValor4() != 0) {
						x.setValor4(r.getValor4());
						x.setContaReduzida4(r.getContaReduzida4());
						x.setContaGradico4(r.getContaGradico4());
						x.setComplemento4(this.complemento4);
						x.setHistorico4(constroiHistoricoRateado(this.complemento4));
					} else if (i == 5 & r.getValor5() != 0) {
						x.setValor5(r.getValor5());
						x.setContaReduzida5(r.getContaReduzida5());
						x.setContaGradico5(r.getContaGradico5());
						x.setComplemento5(this.complemento5);
						x.setHistorico5(constroiHistoricoRateado(this.complemento5));
					} else if (i == 6 & r.getValor6() != 0) {
						x.setValor6(r.getValor6());
						x.setContaReduzida6(r.getContaReduzida6());
						x.setContaGradico6(r.getContaGradico6());
						x.setComplemento6(this.complemento6);
						x.setHistorico6(constroiHistoricoRateado(this.complemento6));
					}
				}
				lista.add(x);
			}

			ReturnUltimoControlRatRN rur = new ReturnUltimoControlRatRN();

			this.cndpagar.setCodigoRateio(rur.getCount());

			this.cndpagar.setRateado(this.rateado);
			boolean valida = false;
			valida = this.fncDAO.adicionaLanctoOmaRateado(this.cndpagar, this.sessaoMB.getUsuario().getEmail(), "oma",
					lista, this.listaArquivos);
			if (valida) {

				this.cndpagar = new cndpagar();
				this.rat = new rateio();

				this.msgAdicinado();
				volta = "lanc-sucesso?faces-redirect=true;";

				RequestContext.getCurrentInstance().execute("PF('dlgObsLancto2').hide();");
			} else {
				this.msgErro();
			}
		}

		return volta;
	}

	public String addLanctoOmaRateadoSIP() {
		String volta = null;
		this.fncDAO = new FinanceiroDAO();

		this.constroiCndpagar();
		/* this.constroiImagem(); */

		List<rateio> lista = new ArrayList<rateio>();

		for (int i = 1; i <= 6; i++) {
			rateio x = new rateio();
			for (rateio r : this.listaDeRateio) {
				if (i == 1 & r.getValor1() != 0) {
					x.setValor1(r.getValor1());
					x.setContaReduzida1(r.getContaReduzida1());
					x.setContaGradico1(r.getContaGradico1());
					x.setComplemento1(this.complemento1);
					x.setHistorico1(constroiHistoricoRateado(this.complemento1));
				} else if (i == 2 & r.getValor2() != 0) {
					x.setValor2(r.getValor2());
					x.setContaReduzida2(r.getContaReduzida2());
					x.setContaGradico2(r.getContaGradico2());
					x.setComplemento2(this.complemento2);
					x.setHistorico2(constroiHistoricoRateado(this.complemento2));
				} else if (i == 3 & r.getValor3() != 0) {
					x.setValor3(r.getValor3());
					x.setContaReduzida3(r.getContaReduzida3());
					x.setContaGradico3(r.getContaGradico3());
					x.setComplemento3(this.complemento3);
					x.setHistorico3(constroiHistoricoRateado(this.complemento3));
				} else if (i == 4 & r.getValor4() != 0) {
					x.setValor4(r.getValor4());
					x.setContaReduzida4(r.getContaReduzida4());
					x.setContaGradico4(r.getContaGradico4());
					x.setComplemento4(this.complemento4);
					x.setHistorico4(constroiHistoricoRateado(this.complemento4));
				} else if (i == 5 & r.getValor5() != 0) {
					x.setValor5(r.getValor5());
					x.setContaReduzida5(r.getContaReduzida5());
					x.setContaGradico5(r.getContaGradico5());
					x.setComplemento5(this.complemento5);
					x.setHistorico5(constroiHistoricoRateado(this.complemento5));
				} else if (i == 6 & r.getValor6() != 0) {
					x.setValor6(r.getValor6());
					x.setContaReduzida6(r.getContaReduzida6());
					x.setContaGradico6(r.getContaGradico6());
					x.setComplemento6(this.complemento6);
					x.setHistorico6(constroiHistoricoRateado(this.complemento6));
				}
			}
			lista.add(x);
		}

		ReturnUltimoControlRatRN rur = new ReturnUltimoControlRatRN();

		this.cndpagar.setCodigoRateio(rur.getCount());

		this.cndpagar.setRateado(this.rateado);
		boolean valida = false;

		if (this.hideSalvar == 1) {
			this.fncDAO.adicionaRateioSIP(this.cndpagar, this.sessaoMB, sessaoMB.getUsuario().getEmail(), lista,
					this.listaArquivos);
		} else {
			valida = this.fncDAO.adicionaLanctoOmaRateado(this.cndpagar, this.sessaoMB.getUsuario().getEmail(), "oma",
					lista, this.listaArquivos);
		}

		if (valida) {

			this.cndpagar = new cndpagar();
			this.rat = new rateio();

			this.msgAdicinado();
			volta = "lanc-sucesso?faces-redirect=true;";

			RequestContext.getCurrentInstance().execute("PF('dlgObsLancto2').hide();");
		} else {
			this.msgErro();
		}

		return volta;
	}

	public void pesquisaContaRat(int val) {
		if (this.nomeConta == null || this.nomeConta.isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Insira o nome da conta para pesquisar!", "Insira o nome da conta para pesquisar!"));
		} else {
			this.fncDAO = new FinanceiroDAO();
			this.lstConta = this.fncDAO.listarPlanoContaNome(this.nomeConta, this.condominio);
			if (this.lstConta.size() == 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Nenhum resultado encontrado!", "Nenhum resultado encontrado!"));
			} else {
				RequestContext.getCurrentInstance().execute("PF('dlgResultadoContaRat" + val + "').show();");
			}
		}
	}

	public void selecionarContaContabilRat(int val) {
		if (val == 1) {
			this.codRed1 = String.valueOf(this.cndplano.getCod_reduzido());
			this.rat.setNomeContaReduzida1(this.cndplano.getNome());
		} else if (val == 2) {
			this.codRed2 = String.valueOf(this.cndplano.getCod_reduzido());
			this.rat.setNomeContaReduzida2(this.cndplano.getNome());
		} else if (val == 3) {
			this.codRed3 = String.valueOf(this.cndplano.getCod_reduzido());
			this.rat.setNomeContaReduzida3(this.cndplano.getNome());
		} else if (val == 4) {
			this.codRed4 = String.valueOf(this.cndplano.getCod_reduzido());
			this.rat.setNomeContaReduzida4(this.cndplano.getNome());
		} else if (val == 5) {
			this.codRed5 = String.valueOf(this.cndplano.getCod_reduzido());
			this.rat.setNomeContaReduzida5(this.cndplano.getNome());
		} else if (val == 6) {
			this.codRed6 = String.valueOf(this.cndplano.getCod_reduzido());
			this.rat.setNomeContaReduzida6(this.cndplano.getNome());
		}

		RequestContext.getCurrentInstance().execute("PF('dlgResultadoContaRat" + val + "').hide();");
		RequestContext.getCurrentInstance().execute("PF('dlgPesqContaRat" + val + "').hide();");
	}

	public void addListaDeRateio() throws ParseException {

		this.listaDeRateio = new ArrayList<>();
		this.fncDAO = new FinanceiroDAO();
		if (this.codRed1 != null) {
			this.rat.setContaReduzida1(Integer.valueOf((this.codRed1.trim().isEmpty() ? 0 : this.codRed1).toString()));
		} else {
			this.rat.setContaReduzida1(0);
		}

		if (this.codRed2 != null) {
			this.rat.setContaReduzida2(Integer.valueOf((this.codRed2.trim().isEmpty() ? 0 : this.codRed2).toString()));
		} else {
			this.rat.setContaReduzida2(0);
		}

		if (this.codRed3 != null) {
			this.rat.setContaReduzida3(Integer.valueOf((this.codRed3.trim().isEmpty() ? 0 : this.codRed3).toString()));
		} else {
			this.rat.setContaReduzida3(0);
		}

		if (this.codRed4 != null) {
			this.rat.setContaReduzida4(Integer.valueOf((this.codRed4.trim().isEmpty() ? 0 : this.codRed4).toString()));
		} else {
			this.rat.setContaReduzida4(0);
		}

		if (this.codRed5 != null) {
			this.rat.setContaReduzida5(Integer.valueOf((this.codRed5.trim().isEmpty() ? 0 : this.codRed5).toString()));
		} else {
			this.rat.setContaReduzida5(0);
		}

		if (this.codRed6 != null) {
			this.rat.setContaReduzida6(Integer.valueOf((this.codRed6.trim().isEmpty() ? 0 : this.codRed6).toString()));
		} else {
			this.rat.setContaReduzida6(0);
		}

		listaDeRateio.add(this.rat);
		boolean valida1 = false;
		boolean valida2 = false;

		for (rateio r : listaDeRateio) {
			if ((r.getValor1() != 0 & r.getContaReduzida1() == 0)
					|| (r.getContaReduzida1() != 0 & r.getValor1() == 0)) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Rateio 1 preenchido errado!", "Rateio 1 preenchido errado!"));
				this.listaDeRateio = null;
			} else if ((r.getValor2() != 0 & r.getContaReduzida2() == 0)
					|| (r.getContaReduzida2() != 0 & r.getValor2() == 0)) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Rateio 2 preenchido errado!", "Rateio 2 preenchido errado!"));
				this.listaDeRateio = null;
			} else if ((r.getValor3() != 0 & r.getContaReduzida3() == 0)
					|| (r.getContaReduzida3() != 0 & r.getValor3() == 0)) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Rateio 3 preenchido errado!", "Rateio 3 preenchido errado!"));
				this.listaDeRateio = null;
			} else if ((r.getValor4() != 0 & r.getContaReduzida4() == 0)
					|| (r.getContaReduzida4() != 0 & r.getValor4() == 0)) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Rateio 4 preenchido errado!", "Rateio 4 preenchido errado!"));
				this.listaDeRateio = null;
			} else if ((r.getValor5() != 0 & r.getContaReduzida5() == 0)
					|| (r.getContaReduzida5() != 0 & r.getValor5() == 0)) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Rateio 5 preenchido errado!", "Rateio 5 preenchido errado!"));
				this.listaDeRateio = null;
			} else if ((r.getValor6() != 0 & r.getContaReduzida6() == 0)
					|| (r.getContaReduzida6() != 0 & r.getValor6() == 0)) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Rateio 6 preenchido errado!", "Rateio 6 preenchido errado!"));
				this.listaDeRateio = null;
			} else {
				valida1 = true;
			}
		}
		boolean aviso1 = true;
		boolean aviso2 = true;
		boolean aviso3 = true;
		boolean aviso4 = true;
		boolean aviso5 = true;
		boolean aviso6 = true;

		if (valida1 == true) {
			for (rateio r : listaDeRateio) {
				if (r.getContaReduzida1() != 0) {
					aviso1 = this.fncDAO.listarPlanoConta2(r.getContaReduzida1(),
							Short.valueOf(String.valueOf(this.condominio)));
				}
				if (r.getContaReduzida2() != 0) {
					aviso2 = this.fncDAO.listarPlanoConta2(r.getContaReduzida2(),
							Short.valueOf(String.valueOf(this.condominio)));
				}
				if (r.getContaReduzida3() != 0) {
					aviso3 = this.fncDAO.listarPlanoConta2(r.getContaReduzida3(),
							Short.valueOf(String.valueOf(this.condominio)));
				}
				if (r.getContaReduzida4() != 0) {
					aviso4 = this.fncDAO.listarPlanoConta2(r.getContaReduzida4(),
							Short.valueOf(String.valueOf(this.condominio)));
				}
				if (r.getContaReduzida5() != 0) {
					aviso5 = this.fncDAO.listarPlanoConta2(r.getContaReduzida5(),
							Short.valueOf(String.valueOf(this.condominio)));
				}
				if (r.getContaReduzida6() != 0) {
					aviso6 = this.fncDAO.listarPlanoConta2(r.getContaReduzida6(),
							Short.valueOf(String.valueOf(this.condominio)));
				}
			}
		}

		if (valida1 == true) {
			if (aviso1 == false) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Conta do 1º rateio está incorreta!", "Conta do 1º rateio está incorreta!"));
			} else if (aviso2 == false) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Conta do 2º rateio está incorreta!", "Conta do 2º rateio está incorreta!"));
			} else if (aviso3 == false) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Conta do 3º rateio está incorreta!", "Conta do 3º rateio está incorreta!"));
			} else if (aviso4 == false) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Conta do 4º rateio está incorreta!", "Conta do 4º rateio está incorreta!"));
			} else if (aviso5 == false) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Conta do 5º rateio está incorreta!", "Conta do 5º rateio está incorreta!"));
			} else if (aviso6 == false) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Conta do 6º rateio está incorreta!", "Conta do 6º rateio está incorreta!"));
			} else {
				valida2 = true;
			}
		} else {
			valida2 = false;
		}

		if (valida2) {
			this.listaDeCodReduzido = new ArrayList<>();
			if (this.codRed1 != null && !this.codRed1.trim().isEmpty()) {
				listaDeCodReduzido.add("R$ " + this.valor1 + ' ' + "Conta: " + this.codRed1);
			}
			if (this.codRed2 != null && !this.codRed2.trim().isEmpty()) {
				listaDeCodReduzido.add("R$ " + this.valor2 + ' ' + "Conta: " + this.codRed2);
			}
			if (this.codRed3 != null && !this.codRed3.trim().isEmpty()) {
				listaDeCodReduzido.add("R$ " + this.valor3 + ' ' + "Conta: " + this.codRed3);
			}
			if (this.codRed4 != null && !this.codRed4.trim().isEmpty()) {
				listaDeCodReduzido.add("R$ " + this.valor4 + ' ' + "Conta: " + this.codRed4);
			}
			if (this.codRed5 != null && !this.codRed5.trim().isEmpty()) {
				listaDeCodReduzido.add("R$ " + this.valor5 + ' ' + "Conta: " + this.codRed5);
			}
			if (this.codRed6 != null && !this.codRed6.trim().isEmpty()) {
				listaDeCodReduzido.add("R$ " + this.valor6 + ' ' + "Conta: " + this.codRed6);
			}
		}

		if (valida2) {
			RequestContext.getCurrentInstance().execute("PF('dlgValRat').hide();");
			this.cndpagar.setValor(this.rat.getValorBruto());
			if (this.hideSalvar == 1) {
				RequestContext.getCurrentInstance().update("frmLancamento:plg25");
				RequestContext.getCurrentInstance().update("frmLancamento:plg27");
				RequestContext.getCurrentInstance().update("frmLancamento:plg28");
			} else {
				RequestContext.getCurrentInstance().update("frmLancamento:panelValRat");
				RequestContext.getCurrentInstance().update("frmLancamento:pnlCtCont");
			}

			this.msgAdicinado();
		}
		this.trataExibicaoHistorico();
	}

	public void cancelarArquivo() {
		this.arquivo = null;
		this.nomeArquivo = "";
		RequestContext.getCurrentInstance().execute("PF('dlgUploadArquivo').hide();");
	}

	public String abrirAlterarLancamento(int val) {
		this.valor = "";
		this.valorBruto = 0.0;
		this.alteracao = true;
		this.validaAlterar = val;
		String alterar = "";
		if (this.cndpagar.getClassificacao() != null) {
			this.classificacao = this.cndpagar.getClassificacao();
		}
		if (this.cndpagar.getVencimento() != null) {
			this.vencimento = this.cndpagar.getVencimento();
		}
		if (this.cndpagar.getValor() != 0) {
			DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));
			this.valor = df.format(this.cndpagar.getValor());
		}

		if (this.cndpagar.getValorLancto() > 0) {
			this.valorGed = this.cndpagar.getValorLancto();
		} else {
			this.valorGed = 0.0;
		}

		if (this.cndpagar.getBloco() != null) {
			this.bloco = this.cndpagar.getBloco();
		}

		if (this.condominio == 4241) {
			this.contaContabil = String.valueOf(this.cndpagar.getConta() - 100000);
		} else {
			this.contaContabil = String.valueOf(this.cndpagar.getConta());
		}

		this.pesquisaContaCod();

		if (this.cndpagar.getTipoDocumento() != null) {
			this.tipoDocumento = this.cndpagar.getTipoDocumento();
		}
		if (this.cndpagar.getCredor() != null) {
			if (!this.cndpagar.getCredor().trim().isEmpty()) {
				this.pesquisaFornecedorUsualcred(this.cndpagar.getCredor());
				this.fornecedor = String.valueOf(this.fornecedorSelecionado.getInscricao());
			}
		}

		if (this.cndpagar.getEmissaoNf() != null) {
			this.dtEmissaoNF = this.cndpagar.getEmissaoNf();
		}
		if (this.cndpagar.getEmpresa() != null) {
			this.empresa = this.cndpagar.getEmpresa();
		}

		if (this.cndpagar.getNumeroNf() != null) {
			if (this.cndpagar.getNumeroNf().bitLength() > 0) {
				this.notaFiscal = String.valueOf(this.cndpagar.getNumeroNf());
			}
		}

		if (this.cndpagar.getHist() != null) {
			this.complemento = this.cndpagar.getHist();
		}

		if (this.cndpagar.getRateado() != null) {
			this.rateado = this.cndpagar.getRateado();
		}

		if (this.cndpagar.getParcelado() != null) {
			if (this.cndpagar.getParcelado().equals("N")) {
				this.parcelamento = "N";
				this.pci = "";
				this.pcf = "";
			} else {
				if (this.cndpagar.getPcInicial() < this.cndpagar.getPcFinal()) {
					this.parcelamento = this.cndpagar.getParcelado();
					if (val == 1) {
						this.pci = String.valueOf(this.cndpagar.getPcInicial() + 1);
					} else {
						this.pci = String.valueOf(this.cndpagar.getPcInicial());
					}
					this.pcf = String.valueOf(this.cndpagar.getPcFinal());
				}
			}
		} else {
			this.parcelamento = "N";
			this.pci = "";
			this.pcf = "";
		}

		// ---------------------------------------------------//

		if (this.cndpagar.getRateado().equals("S")) {
			this.fncDAO = new FinanceiroDAO();
			this.lstRateioAlteracao = this.fncDAO.listarContasRateadas(this.cndpagar.getCodigoRateio());
			int contador = 1;
			for (cndpagar aux : this.lstRateioAlteracao) {
				switch (contador) {
				case 1:
					this.valor1 = String.valueOf(aux.getValor());
					this.codRed1 = String.valueOf(aux.getConta());
					pesquisaContaRatCod1();
					break;
				case 2:
					this.valor2 = String.valueOf(aux.getValor());
					this.codRed2 = String.valueOf(aux.getConta());
					pesquisaContaRatCod2();
					break;
				case 3:
					this.valor3 = String.valueOf(aux.getValor());
					this.codRed3 = String.valueOf(aux.getConta());
					pesquisaContaRatCod3();
					break;
				case 4:
					this.valor4 = String.valueOf(aux.getValor());
					this.codRed4 = String.valueOf(aux.getConta());
					pesquisaContaRatCod4();
					break;
				case 5:
					this.valor5 = String.valueOf(aux.getValor());
					this.codRed5 = String.valueOf(aux.getConta());
					pesquisaContaRatCod5();
					break;
				case 6:
					this.valor6 = String.valueOf(aux.getValor());
					this.codRed6 = String.valueOf(aux.getConta());
					pesquisaContaRatCod6();
					break;
				default:
					break;
				}
				contador++;
				this.valorBruto += aux.getValor();
			}
			this.qtdeDeContas = this.lstRateioAlteracao.size();
			this.valor = String.valueOf(this.valorBruto);
			this.valorGed = this.valorBruto;
		}

		// SEGUNDA TELA TIPO DE PAGAMENTO
		if (this.cndpagar.getCodigoBarra() != null) {
			this.codigoBarras = this.cndpagar.getCodigoBarra();
			this.tipoPagamento = this.cndpagar.getTipoPagto();
		}
		if (this.cndpagar.getTipoPagto() != null) {
			this.fncDAO = new FinanceiroDAO();
			String cpfcnpj = "";
			if (this.cndpagar.getTipoPagto().equals("5")) {

				this.nomeFavorecido = this.cndpagar.getFavorecido();
				this.codAgencia = String.valueOf(this.cndpagar.getAgencDestino());
				this.codBanco = String.valueOf(this.cndpagar.getBancoDestino());
				this.pesquisarBanco();
				this.cc = this.cndpagar.getContaDestino();
				this.dac = this.cndpagar.getDigAgeDest();
				this.tipoPessoa = this.cndpagar.getTipoPessoa();
				this.cpf_cnpj = this.converte((long) this.cndpagar.getCnpj());
				this.contaPoupanca = this.cndpagar.getTipoContaBancaria();

				if (this.tipoPessoa.trim().equals("F")) {
					while (this.cpf_cnpj.length() < 11) {
						cpfcnpj = "0";
						this.cpf_cnpj = cpfcnpj + this.cpf_cnpj;
					}
				} else if (this.tipoPessoa.trim().equals("J")) {
					while (this.cpf_cnpj.length() < 14) {
						cpfcnpj = "0";
						this.cpf_cnpj = cpfcnpj + this.cpf_cnpj;
					}
				}

				List<cpfavor> lstFavor = this.fncDAO.listarFavorecidoCodigo(this.cndpagar.getCodigoFav());
				if (lstFavor.size() > 0) {
					this.favorecidoSelecionado = lstFavor.get(0);
				}

			} else if (this.cndpagar.getTipoPagto().equals("7")) {
				this.nomeFavorecido = this.cndpagar.getFavorecido();
				this.codAgencia = String.valueOf(this.cndpagar.getAgencDestino());
				this.codBanco = String.valueOf(this.cndpagar.getBancoDestino());
				this.pesquisarBanco();
				this.cc = this.cndpagar.getContaDestino();
				this.dac = this.cndpagar.getDigAgeDest();
				this.tipoPessoa = this.cndpagar.getTipoPessoa();
				this.cpf_cnpj = this.converte((long) this.cndpagar.getCnpj());
				this.contaPoupanca = this.cndpagar.getTipoContaBancaria();

				if (this.tipoPessoa.trim().equals("F")) {
					while (this.cpf_cnpj.length() < 11) {
						cpfcnpj = "0";
						this.cpf_cnpj = cpfcnpj + this.cpf_cnpj;
					}
				} else if (this.tipoPessoa.trim().equals("J")) {
					while (this.cpf_cnpj.length() < 14) {
						cpfcnpj = "0";
						this.cpf_cnpj = cpfcnpj + this.cpf_cnpj;
					}
				}

				List<cpfavor> lstFavor = this.fncDAO.listarFavorecidoCodigo(this.cndpagar.getCodigoFav());
				if (lstFavor.size() > 0) {
					this.favorecidoSelecionado = lstFavor.get(0);
				}

			} else if (this.cndpagar.getTipoPagto().equals("8")) {
				this.listarLinhaDigitavel();
			} else if (this.cndpagar.getTipoPagto().equals("E")) {
				this.listarLinhaDigitavel();
			}
		}
		this.listaArquivos = new ArrayList<>();
		if (this.cndpagar.getImagens() != null) {
			this.listaArquivos.addAll(this.cndpagar.getImagens());
		}
		this.lstExcArquivo = new ArrayList<>();
		alterar = "financeiro?faces-redirect=true;";
		return alterar;
	}

	public void pesquisaFornecedorUsualcred(String usualcred) {
		if (usualcred != null) {
			this.fncDAO = new FinanceiroDAO();
			this.fornecedorSelecionado = this.fncDAO.pesquisaFornecedorUsualcred(usualcred);
			if (this.fornecedorSelecionado != null && !this.fornecedorSelecionado.getNome().isEmpty()) {
				this.nomeFornecedor = this.fornecedorSelecionado.getNome();
			} else {
				this.nomeFornecedor = "";
				this.fornecedor = "";
				this.fornecedorSelecionado = null;
			}
		}
	}

	public String alterarLancamento() {
		this.cndpagar.setUsuarioAprovacao(null);
		String alterar = "";
		if (this.cndpagar.getCodigo() == 0) {
			this.msgErro();
		} else {
			this.fncDAO = new FinanceiroDAO();
			this.constroiCndpagar();
			if (this.cndpagar.getRateado().equals("N")) {

				// REMOVE APROVADORES APÓS ALTERAÇÃO
				if (this.cndpagar.getParcelado().equals("S") && this.validaAlterar == 1) {
					this.cndpagar = this.fncDAO.alterarLancamentoParcelado(this.cndpagar,
							this.sessaoMB.getUsuario().getEmail(), "oma", this.lstExcArquivo, this.listaArquivos,
							this.obsLancto);
				} else {
					List<cndpagar_aprovacao> listaAprovadores = new ArrayList<cndpagar_aprovacao>();
					listaAprovadores.addAll(this.cndpagar.getAprovadores());
					this.cndpagar.getAprovadores().clear();
					this.fncDAO.alterarLancamento(this.cndpagar, this.sessaoMB.getUsuario().getEmail(), "oma",
							this.lstExcArquivo, this.listaArquivos, this.obsLancto, this.validaAlterar);
					this.fncDAO.removerAprovacoes(listaAprovadores);
				}
			} else {
				for (cndpagar aux : this.lstRateioAlteracao) {
					cndpagar lancto = (cndpagar) this.cndpagar.clone();
					lancto.setConta(aux.getConta());
					lancto.setValor(aux.getValor());
					lancto.setCodigo(aux.getCodigo());

					// REMOVE APROVADORES APÓS ALTERAÇÃO
					List<cndpagar_aprovacao> listaAprovadores = new ArrayList<cndpagar_aprovacao>();
					listaAprovadores.addAll(lancto.getAprovadores());
					lancto.getAprovadores().clear();
					this.fncDAO.alterarLancamento(lancto, this.sessaoMB.getUsuario().getEmail(), "oma",
							this.lstExcArquivo, this.listaArquivos, this.obsLancto, this.validaAlterar);
					this.fncDAO.removerAprovacoes(listaAprovadores);
					lancto = new cndpagar();
				}
			}
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Alterado com sucesso!", "Alterado com sucesso!"));
			int cdLancamento = this.cndpagar.getCodigo();
			this.reset();
			this.alteracao = false;
			this.grid1 = true;
			this.grid2 = false;
			this.grid3 = false;
			this.cdFinancImagem = 0;
			this.listaArquivos = null;
			this.lstExcArquivo = null;
			this.fncDAO = new FinanceiroDAO();
			this.cndpagar = this.fncDAO.pesquisarLancamento(cdLancamento);
			this.lstRateioAlteracao = null;
			this.obsLancto = null;
			RequestContext.getCurrentInstance().execute("PF('dlgObsLancto1').hide()");
			RequestContext.getCurrentInstance().execute("PF('dlgObsLancto2').hide()");
		}
		alterar = "financeiro?faces-redirect=true;";
		return alterar;
	}

	public void reset() {
		cndpagar = new cndpagar();
		fornecedorSelecionado = null;
		favorecidoSelecionado = null;
		lstFornecedor = null;
		lstFavorecido = null;
		lstConta = null;
		lstLancamentos = null;
		grid1 = true;
		grid2 = false;
		grid3 = false;
		usarTED = false;
		codigoConta = 0;
		idLancamento = null;
		vencimento = null;
		contaContabil = null;
		notaFiscal = null;
		codBanco = null;
		codAgencia = null;
		codMovimento = null;
		codCompensacao = null;
		ldCampo1 = null;
		ldCampo2 = null;
		ldCampo3 = null;
		ldDac = null;
		ldValor = null;
		concSegbarra1 = null;
		concSegbarra2 = null;
		concSegbarra3 = null;
		concSegbarra4 = null;
		valor = null;
		fornecedor = null;
		complemento = null;
		empresa = null;
		tipoPagamento = null;
		favorecido = null;
		idImagem = null;
		cc = null;
		dac = null;
		tipoPessoa = null;
		cpf_cnpj = null;
		tipoPagto = null;
		codigoBarras = null;
		this.listaArquivos = new ArrayList<financeiro_imagem>();
		arquivoDownload = null;
		nomeConta = null;
		this.nomeFornecedor = null;
		this.dtEmissaoNF = null;
		this.flagObs = false;
		this.lstRateioAlteracao = null;
		// RequestContext.getCurrentInstance().reset("frmLancamento");
	}

	public void abrirGridCompletarAprovacao() {
		if (this.cndpagar.getClassificacao() != null) {
			this.classificacao = this.cndpagar.getClassificacao();
		} else {
			this.classificacao = null;
		}

		if (this.cndpagar.getVencimento() != null) {
			this.vencimento = this.cndpagar.getVencimento();
		} else {
			this.vencimento = null;
		}

		if (this.cndpagar.getTipoDocumento() != null) {
			this.tipoDocumento = this.cndpagar.getTipoDocumento();
		} else {
			this.tipoDocumento = null;
		}

		if (this.cndpagar.getEmissaoNf() != null) {
			this.dtEmissaoNF = this.cndpagar.getEmissaoNf();
		} else {
			this.dtEmissaoNF = null;
		}

		if (this.cndpagar.getEmpresa() != null) {
			this.empresa = this.cndpagar.getEmpresa();
		} else {
			this.empresa = null;
		}

		if (this.cndpagar.getNumeroNf() != null) {
			this.notaFiscal = String.valueOf(this.cndpagar.getNumeroNf());
		} else {
			this.notaFiscal = null;
		}

		if (this.cndpagar.getHist() != null) {
			this.complemento = this.cndpagar.getHist();
		}

		if (this.cndpagar.getConta() > 0) {

			if (this.condominio == 4241) {
				this.contaContabil = String.valueOf(this.cndpagar.getConta() - 100000);
			} else {
				this.contaContabil = String.valueOf(this.cndpagar.getConta());
			}

			this.pesquisaContaCod();
		} else {
			this.contaContabil = null;
			this.nomeConta = null;
			// this.nomeCapa = null;
		}
	}

	public void constroiLanctoAprovacaoResumido() {
		this.cndpagar.setUsuarioAprovacao(null);
		if (this.classificacao != null) {
			this.cndpagar.setClassificacao(this.classificacao);
		} else {
			this.cndpagar.setClassificacao(null);
		}
		if (this.contaContabil != null && !this.contaContabil.trim().isEmpty()) {
			this.cndpagar.setConta(Integer.parseInt(this.contaContabil));
			this.cndpagar.setCtaAnlFinanc(Integer.valueOf(this.cndplano.getCodigo_grafico()));
		} else {
			this.cndpagar.setConta(0);
			this.cndpagar.setCtaAnlFinanc(0);
		}
		if (this.dtEmissaoNF != null) {
			this.cndpagar.setEmissaoNf(this.dtEmissaoNF);
		} else {
			this.cndpagar.setEmissaoNf(null);
		}
		StringBuilder strBuilder = new StringBuilder();
		String historico = null;
		if (this.empresa != null && !this.empresa.isEmpty()) {
			strBuilder.append(this.empresa);
			this.cndpagar.setEmpresa(this.empresa);
		} else {
			this.cndpagar.setEmpresa(null);
		}
		if (this.notaFiscal != null && !this.notaFiscal.isEmpty()) {
			this.cndpagar.setNumeroNf(BigInteger.valueOf(Long.parseLong(this.notaFiscal)));
			this.cndpagar.setNf(this.notaFiscal);
			if (this.empresa != null && !this.empresa.isEmpty()) {
				strBuilder.append(" - ");
			}
			if (!this.notaFiscal.trim().isEmpty()) {
				strBuilder.append(this.tipoDocumento + " " + this.notaFiscal);
			}
		}
		if (this.complemento != null && !this.complemento.isEmpty()) {
			if ((this.notaFiscal != null && !this.notaFiscal.isEmpty())
					|| (this.empresa != null && !this.empresa.isEmpty())) {
				strBuilder.append(" - ");
			}
			strBuilder.append(this.complemento);
			this.cndpagar.setHist(this.complemento);
		} else {
			this.cndpagar.setHist(null);
		}
		historico = strBuilder.toString();
		this.cndpagar.setHistorico(historico);
	}

	public boolean verificaCamposCompletar() {
		if (this.contaContabil.trim().isEmpty()) {
			return false;
		}
		if (this.empresa.trim().isEmpty()) {
			return false;
		}
		if (this.notaFiscal.trim().isEmpty()) {
			return false;
		}
		if (this.dtEmissaoNF == null) {
			return false;
		}
		if (this.complemento.trim().isEmpty()) {
			return false;
		}
		return true;
	}

	public void pesquisaConta2() {
		if (this.nomeConta == null || this.nomeConta.isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Insira o nome da conta para pesquisar!", "Insira o nome da conta para pesquisar!"));
		} else {
			this.fncDAO = new FinanceiroDAO();
			this.lstConta = this.fncDAO.listarPlanoContaNome(this.nomeConta, this.condominio);
			if (this.lstConta.size() == 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Nenhum resultado encontrado!", "Nenhum resultado encontrado!"));
			} else {
				RequestContext.getCurrentInstance().execute("PF('dlgResultadoConta2').show();");
			}
		}
	}

	public boolean validaCPF() {
		String cpfcnpj = "";
		this.validaCpfCnpj = new ValidaCPFCNPJ();
		if (this.tipoPessoa.trim().equals("F")) {
			while (this.cpf_cnpj.length() < 11) {
				cpfcnpj += "0";
				this.cpf_cnpj = cpfcnpj + this.cpf_cnpj;
			}
		} else if (this.tipoPessoa.trim().equals("J")) {
			while (this.cpf_cnpj.length() < 14) {
				cpfcnpj += "0";
				this.cpf_cnpj = cpfcnpj + this.cpf_cnpj;
			}
		}

		if (!this.tipoPagamento.trim().equals("5") && !this.tipoPagamento.trim().equals("7")) {
			return true;
		}

		if (this.cpf_cnpj.length() > 12) {
			if (this.cpf_cnpj != null && !this.cpf_cnpj.trim().isEmpty()) {
				if (validaCpfCnpj.validaCNPJ(this.cpf_cnpj)) {
					/*
					 * FacesContext.getCurrentInstance().addMessage(null, new
					 * FacesMessage(FacesMessage.SEVERITY_INFO,
					 * "CNPJ Favorecido válido !", "CNPJ Favorecido válido !"));
					 * if (this.tipoPagamento.trim().trim().equals("7")) {
					 * RequestContext.getCurrentInstance().update(
					 * "frmLancamento:msg5"); } else if
					 * (this.tipoPagamento.trim().equals("5")) {
					 * RequestContext.getCurrentInstance().update(
					 * "frmLancamento:msg6"); }
					 */
					return true;
				} else {
					/*
					 * FacesContext.getCurrentInstance().addMessage(null, new
					 * FacesMessage(FacesMessage.SEVERITY_WARN,
					 * "CNPJ Favorecido inválido !",
					 * "CNPJ Favorecido inválido !")); if
					 * (this.tipoPagamento.trim().trim().equals("7")) {
					 * RequestContext.getCurrentInstance().update(
					 * "frmLancamento:msg5"); } else if
					 * (this.tipoPagamento.trim().equals("5")) {
					 * RequestContext.getCurrentInstance().update(
					 * "frmLancamento:msg6"); }
					 */
					return false;
				}
			}
		} else {
			if (this.cpf_cnpj != null && !this.cpf_cnpj.trim().isEmpty()) {
				if (validaCpfCnpj.validaCPF(this.cpf_cnpj)) {
					/*
					 * FacesContext.getCurrentInstance().addMessage(null, new
					 * FacesMessage(FacesMessage.SEVERITY_INFO,
					 * "CPF Favorecido válido !", "CPF Favorecido válido !"));
					 * if (this.tipoPagamento.trim().trim().equals("7")) {
					 * RequestContext.getCurrentInstance().update(
					 * "frmLancamento:msg5"); } else if
					 * (this.tipoPagamento.trim().equals("5")) {
					 * RequestContext.getCurrentInstance().update(
					 * "frmLancamento:msg6"); }
					 */
					return true;
				} else {
					/*
					 * FacesContext.getCurrentInstance().addMessage(null, new
					 * FacesMessage(FacesMessage.SEVERITY_WARN,
					 * "CPF Favorecido inválido !", "CPF Favorecido inválido !"
					 * )); if (this.tipoPagamento.trim().trim().equals("7")) {
					 * RequestContext.getCurrentInstance().update(
					 * "frmLancamento:msg5"); } else if
					 * (this.tipoPagamento.trim().equals("5")) {
					 * RequestContext.getCurrentInstance().update(
					 * "frmLancamento:msg6"); }
					 */
					return false;
				}
			}
		}

		return false;
	}

	public void validaCPFIcon() {
		String cpfcnpj = "";
		this.validaCpfCnpj = new ValidaCPFCNPJ();
		if (this.tipoPessoa.trim().equals("F")) {
			while (this.cpf_cnpj.length() < 11) {
				cpfcnpj += "0";
				this.cpf_cnpj = cpfcnpj + this.cpf_cnpj;
			}
		} else if (this.tipoPessoa.trim().equals("J")) {
			while (this.cpf_cnpj.length() < 14) {
				cpfcnpj += "0";
				this.cpf_cnpj = cpfcnpj + this.cpf_cnpj;
			}
		}
		if (!this.tipoPagamento.trim().equals("5") && !this.tipoPagamento.trim().equals("7")) {
			this.checkCPF = 2;
		}
		if (this.cpf_cnpj.length() > 12) {
			if (this.cpf_cnpj != null && !this.cpf_cnpj.trim().isEmpty()) {
				if (validaCpfCnpj.validaCNPJ(this.cpf_cnpj)) {
					this.checkCPF = 2;
				} else {
					this.checkCPF = 1;
				}
			}
		} else {
			if (this.cpf_cnpj != null && !this.cpf_cnpj.trim().isEmpty()) {
				if (validaCpfCnpj.validaCPF(this.cpf_cnpj)) {
					this.checkCPF = 2;
				} else {
					this.checkCPF = 1;
				}
			}
		}
	}

	public void listarHistPadrao() {
		if (this.codigoHistPadrao != null && !this.codigoHistPadrao.trim().isEmpty()) {
			this.fncDAO = new FinanceiroDAO();
			String valor = this.fncDAO.listarHistoricoPadrao(Integer.valueOf(this.codigoHistPadrao));
			if (valor != null && !valor.trim().isEmpty()) {
				this.complemento = valor;
			} else {
				this.msgHistPadrao = "Nenhuma histórico encontrada!";
			}
		}
	}

	public String listarFornecedor(String credor) {
		this.fncDAO = new FinanceiroDAO();
		String nome = "";
		if (!credor.trim().isEmpty()) {
			nome = this.fncDAO.listarCredorNome(credor).toString();
		}
		return nome;
	}

	public String listarFornecedorCNPJ(String credor) {
		this.fncDAO = new FinanceiroDAO();
		String cnpj = "";
		if (!credor.trim().isEmpty()) {
			cnpj = this.fncDAO.listarCredorCNPJ(credor).toString();
		}
		return cnpj;
	}

	public boolean validaSalvarLanctoSIP() {
		if (this.tipoPagamento.equals("5") || this.tipoPagamento.equals("7")) {
			if (this.nomeFavorecido.trim().isEmpty() || this.codBanco.trim().isEmpty()
					|| this.codAgencia.trim().isEmpty() || this.cc.trim().isEmpty() || this.tipoPessoa.trim().isEmpty()
					|| this.cpf_cnpj.trim().isEmpty() || this.contaPoupanca.trim().isEmpty()) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Preencha todos os dados no tipo de pagamento!",
								"Preencha todos os dados no tipo de pagamento!"));
				RequestContext.getCurrentInstance().update("frmPreLancto:msg0");
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}

	}

	public void salvarLanctoSIP() throws ParseException {
		try {
			FinanceiroImpostosDAO fiDAO = new FinanceiroImpostosDAO();
			this.cndpagar.setUsuarioAprovacao(null);
			if (this.fornecedor == null || this.fornecedor.isEmpty()) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Fornecedor não informado!", "Fornecedor não informado!"));
				RequestContext.getCurrentInstance().update("frmPreLancto:msg0");
			} else {

				if (this.validaSalvarLanctoSIP()) {
					this.constroiCndpagar();
					if (this.retornoCPFCNPJ()) {
						if (this.cndpagar.getConta() == 0) {
							FacesContext.getCurrentInstance().addMessage(null,
									new FacesMessage(FacesMessage.SEVERITY_WARN, "Conta contabil não informada!",
											"Conta contabil não informada!"));
							RequestContext.getCurrentInstance().update("frmPreLancto:msg0");
						} else {

							if (this.hideSalvar == 1) {
								this.cndpagar.setStatusSIP(4);
								this.cndpagar.setFeitoLanctoSIP(new Date());
							} else if (this.hideSalvar == 2) {
								this.cndpagar.setStatusSIP(5);
								this.cndpagar.setVistoGerente(true);
								this.cndpagar.setFeitoGerenteSIP(new Date());
							}

							boolean retorno = false;
							this.blackListMB.setCondominio(this.cndpagar.getCondominio());
							this.blackListMB.setCodigoGerente(this.cndpagar.getCodigoGerente());
							this.blackListMB.setContaContabil(Integer.valueOf(this.contaContabil));
							this.blackListMB.setCnpj(Double.valueOf(this.cndpagar.getFornecedorCnpj()));

							retorno = fiDAO.verificaBlackList(this.blackListMB);

							if (retorno) {
								if (this.obsLancto == null) {
									this.obsLancto = "Feito pelo Lançamento - Lançamento suspenso pela BlackList";
								} else {
									this.obsLancto = "Feito Lançamento -" + this.obsLancto
											+ "- Lançamento suspenso pela BlackList";
								}
								this.cndpagar.setSuspensoGerente(3);
							}
							this.cndpagar.setEstimado("P");

							if (!retorno) {
								boolean salvar;
								this.fncDAO.excluirLanctoSIGA(this.cndpagar);
								salvar = this.salvarSIPSIGA();
								if (salvar) {
									this.fncDAO.adicionaLanctoSIP(this.cndpagar, this.sessaoMB, this.obsLancto);

									if (this.cndpagar.getReterImposto() != null
											&& this.cndpagar.getReterImposto().equals("S")) {
										this.fncDAO.updateTributoImpos(this.cndpagar);
									}

									FacesContext.getCurrentInstance().addMessage(null,
											new FacesMessage(FacesMessage.SEVERITY_INFO, "Salvo!", "Salvo!"));
									RequestContext.getCurrentInstance().update("frmPreLancto:msg0");

									this.listarCndpagarContas = null;
									this.listarCndpagarGerente = null;
									this.obsLancto = "";
									this.voltar();
								} else {
									this.cndpagar.setStatusSIP(3);
									FacesContext.getCurrentInstance().addMessage(null,
											new FacesMessage(FacesMessage.SEVERITY_ERROR,
													"Erro - Não foi possível salvar esse Lançamento!",
													"Erro - Não foi possível salvar esse Lançamento!"));
									RequestContext.getCurrentInstance().update("frmPreLancto:msg5");
								}
							} else {
								this.fncDAO.adicionaLanctoSIP(this.cndpagar, this.sessaoMB, this.obsLancto);

								if (this.cndpagar.getReterImposto() != null
										&& this.cndpagar.getReterImposto().equals("S")) {
									this.fncDAO.updateTributoImpos(this.cndpagar);
								}

								FacesContext.getCurrentInstance().addMessage(null,
										new FacesMessage(FacesMessage.SEVERITY_INFO, "Salvo!", "Salvo!"));
								RequestContext.getCurrentInstance().update("frmPreLancto:msg0");

								this.listarCndpagarContas = null;
								this.listarCndpagarGerente = null;
								this.obsLancto = "";
								this.voltar();
							}

						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean verificaBloqueio(cndpagar pagar) {
		BlackListDAO blDAO = new BlackListDAO();
		intra_condominios condoMB = new intra_condominios();
		black_list blMB = new black_list();

		condoMB.setCodigo(pagar.getCondominio());
		blMB.setCodigoGerente(pagar.getCodigoGerente());
		blMB.setContaContabil(pagar.getConta());
		blMB.setCnpj(pagar.getCnpj());

		if (blDAO.verificaCondo(condoMB)) {
			return true;
		} else if (blDAO.verificaTodos(condoMB, blMB)) {
			return true;
		} else if (blDAO.verificaCondoCNPJ(condoMB, blMB)) {
			return true;
		} else if (blDAO.verificaCondoContabil(condoMB, blMB)) {
			return true;
		} else if (blDAO.verificaCNPJContabil(blMB, pagar)) {
			return true;
		} else if (blDAO.verificaCNPJ(blMB, pagar)) {
			return true;
		} else if (blDAO.verificaContabil(blMB, pagar)) {
			return true;
		} else {
			return false;
		}

	}

	public void excluirLancamentoSIP() {
		this.fncDAO = new FinanceiroDAO();
		if (this.pagarLS != null && this.pagarLS.getCodigo() > 0) {
			List<cndpagar_aprovacao> listaAprovadores = null;

			if (this.pagarLS.getCodigoRateio() > 0 && this.pagarLS.getRateado().equals("S")) {
				List<cndpagar> lista = fncDAO.listaDeCndpagarRateado(this.pagarLS);
				for (cndpagar aux : lista) {
					listaAprovadores = new ArrayList<cndpagar_aprovacao>();
					listaAprovadores.addAll(aux.getAprovadores());
				}
			}

			this.fncDAO.excluirLancamento(this.pagarLS, this.sessaoMB.getUsuario().getEmail(), "oma", this.obsLancto);
			this.fncDAO.excluirLanctoSIGA(this.pagarLS);

			RequestContext.getCurrentInstance().execute("PF('dlgExclui').hide();");
			this.listaDePagar = null;
			this.filtroDePagar = null;
			this.listarGed = null;
			this.listarCndpagarContas = null;
			this.listarCndpagarGerente = null;
			this.cdFinancImagem = 0;
			this.obsLancto = null;
			this.msgExclusao();
			RequestContext.getCurrentInstance().execute("frmLancto:msg0");
		} else {
			this.msgRegistro();
		}
	}

	public boolean retornoCPFCNPJ() {
		if (this.cndpagar.getTipoPagto().trim().equals("5") || this.cndpagar.getTipoPagto().trim().equals("7")) {
			if (this.cpf_cnpj == null) {
				return true;
			} else {
				return this.validaCPF();
			}
		} else {
			return true;
		}
	}

	public void avancar(cndpagar pagar, int valor) {
		this.fncDAO = new FinanceiroDAO();
		this.proxima1 = false;
		this.proxima2 = true;
		this.cndpagar = this.fncDAO.pesqLancto(pagar.getCodigo());
		this.abrirAlterarLancamento(2);
		this.hideSalvar = valor;

	}

	public void avancar2(cndpagar pagar, int valor) {
		this.fncDAO = new FinanceiroDAO();
		this.proxima1 = false;
		this.proxima2 = true;
		this.cndpagar = pagar;
		this.abrirAlterarLancamento(2);
		this.hideSalvar = valor;

	}

	public void voltar() {
		this.proxima1 = true;
		this.proxima2 = false;
		this.cndpagar = new cndpagar();
		this.listarCndpagarGerente = null;
		this.cdImagem = 0;
		this.codigoHistPadrao = "";
		this.fncDAO = new FinanceiroDAO();
		this.reset();
	}

	public boolean validaCampos() {
		boolean valida = true;

		if (this.cndpagar.getTipoPagto().trim().equals("5")) {

		}
		if (this.cndpagar.getCnpj() < 0) {
			valida = false;
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Informe o CNPJ!", "Informe o CNPJ!"));
			RequestContext.getCurrentInstance().update("frmPreLancto:msg0");
		} else if (this.cndpagar.getVencimento() == null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Informe o Vencimento!", "Informe o Vencimento!"));
			RequestContext.getCurrentInstance().update("frmPreLancto:msg0");
		}

		return valida;
	}

	public boolean validaTipo5() {
		boolean tipo5 = true;

		return tipo5;
	}

	public void bloquearLiberar(cndpagar pagar, int valor, int setor) {
		pagar.setUsuarioAprovacao(null);
		this.fncDAO = new FinanceiroDAO();
		this.fncDAO.liberarPagamento(pagar, valor, this.sessaoMB, setor);
		if (valor == 1) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Liberado!", "Liberado!"));
		} else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Suspenso!", "Suspenso!"));
		}
		RequestContext.getCurrentInstance().update("frmPreLancto:msg0");
		this.listarCndpagarContas = null;
		this.listarCndpagarGerente = null;
	}

	public String alertaBloqueio(int valor) {
		if (valor == 1) {
			return "";
		} else {
			return "alertaBloqueio";
		}
	}

	public void salvarVisto(cndpagar visto) {
		this.fncDAO = new FinanceiroDAO();
		this.cndpagar = visto;

		this.cndpagar.setUsuarioAprovacao(null);
		this.cndpagar.setStatusSIP(5);
		this.cndpagar.setVistoGerente(true);
		this.cndpagar.setFeitoGerenteSIP(new Date());
		this.cndpagar.setEstimado("P");
		this.fncDAO.adicionaLanctoSIP(this.cndpagar, this.sessaoMB, this.obsLancto);

		this.fncDAO.updateSGLTIMP(this.cndpagar);
		this.fncDAO.updateSGIMPOS(this.cndpagar);

		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Salvo!", "Salvo!"));
		RequestContext.getCurrentInstance().update("frmPreLancto:msg0");
		this.listarCndpagarGerente = null;
		this.obsLancto = "";
	}

	public void listarLancto() {
		DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
				.findComponent("frmPreLancto:dtLancto");
		d.setValue(null);
		this.listarCndpagarContas = null;
		this.listarCndpagarGerente = null;
		this.filtroCndpagarGerente = null;
		this.filtroCndpagarContas = null;
		RequestContext.getCurrentInstance().execute("$('.ui-column-filter').val('');");
	}

	public void suspenderLiberar(cndpagar p) {
		this.pagarLS = p;
	}

	public void suspenderLiberarLancamento(int acao, int setor) {
		this.pagarLS.setUsuarioAprovacao(null);
		this.fncDAO = new FinanceiroDAO();
		if (setor == 1) {
			if (acao == 1) {
				this.fncDAO.suspenderLiberarLancto(this.pagarLS, acao, setor, this.sessaoMB, this.obsLancto, this.imagemSelecionada.getId());
				this.listarCndpagarContas = null;
				this.listarCndpagarGerente = null;
				this.obsLancto = "";
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Suspenso", "Suspenso"));
				this.voltar();
			} else if (acao == 2) {
				this.fncDAO.suspenderLiberarLancto(this.pagarLS, acao, setor, this.sessaoMB, this.obsLancto, this.imagemSelecionada.getId());
				this.listarCndpagarContas = null;
				this.listarCndpagarGerente = null;
				this.obsLancto = "";
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Liberado", "Liberado"));
				this.voltar();
			}
		} else if (setor == 2) {
			if (acao == 1) {
				this.fncDAO.suspenderLiberarLancto(this.pagarLS, acao, setor, this.sessaoMB, this.obsLancto, this.imagemSelecionada.getId());
				this.fncDAO.excluirTributos(this.pagarLS);
				this.listarCndpagarContas = null;
				this.listarCndpagarGerente = null;
				this.obsLancto = "";
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Suspenso", "Suspenso"));
				this.voltar();
			} else if (acao == 2) {
				this.fncDAO.suspenderLiberarLancto(this.pagarLS, acao, setor, this.sessaoMB, this.obsLancto, this.imagemSelecionada.getId());
				this.listarCndpagarContas = null;
				this.listarCndpagarGerente = null;
				this.obsLancto = "";
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Liberado", "Liberado"));
				this.voltar();
			}
		}
	}

	public void liberarPagamentoBoleto(int acao, int setor) {
		try {
			this.fncDAO = new FinanceiroDAO();
			this.fncDAO.liberarPagBoleto(this.cndpagar, this.sessaoMB, this.obsLancto);
			this.listarCndpagarContas = null;
			this.listarCndpagarGerente = null;
			this.obsLancto = "";
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Liberado", "Liberado"));
			this.voltar();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void reprovarSIPLancamento(int setor) {
		this.cndpagar.setUsuarioAprovacao(null);
		this.fncDAO = new FinanceiroDAO();
		this.cndpagar.setReprovadoGerente(1);
		this.fncDAO.reprovarSipLancamento(this.cndpagar, setor, this.sessaoMB, this.obsLancto);

		this.fncDAO.excluirLanctoSIGA(this.cndpagar);

		this.fncDAO.excluirTributos(this.cndpagar);

		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Reprovado!", "Reprovado!"));
		this.listarCndpagarContas = null;
		this.listarCndpagarGerente = null;
		this.obsLancto = "";
		this.voltar();
	}

	public int listarSuspensoContas() {
		this.fncDAO = new FinanceiroDAO();
		int valor = this.fncDAO.listarSuspensoContas();
		return valor;
	}

	public int listarSuspensoGerente() {
		this.fncDAO = new FinanceiroDAO();
		int valor = this.fncDAO.listarSuspensoGerente();
		return valor;
	}

	public boolean salvarSIPSIGA() {
		boolean retorno = false;
		try {
			this.cndpagar.setUsuarioAprovacao(null);
			this.fncDAO = new FinanceiroDAO();
			FinanceiroSIPDAO sipDAO = new FinanceiroSIPDAO();
			if (this.cndpagar.getCondominio() == 4241) {
				short contaGrau1 = this.fncDAO.listarContaBancaria(this.cndpagar.getConta());
				this.cndpagar.setContaBancaria(contaGrau1);
			}
			// String val = this.fncDAO.adicionarLanctoSiga(this.cndpagar,
			// sessaoMB.getUsuario().getEmail(), "oma", this.obsLancto);
			this.cndpagar.setEstimado("P");
			retorno = sipDAO.adicionarLanctoSigaLiberar(this.cndpagar, sessaoMB, "oma", this.obsLancto, this.imagemSelecionada.getId());

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, e.getMessage(), e.getMessage()));
			e.printStackTrace();
		}
		return retorno;
	}

	public int quantidadeSuspensoLancto() {
		this.fncDAO = new FinanceiroDAO();
		int valor = this.fncDAO.quantidadeSuspensoLancto();
		return valor;
	}

	public int quantidadeSuspensoGerente() {
		int valor = 0;
		if (this.sessaoMB.getGerenteSelecionado() != null) {
			this.fncDAO = new FinanceiroDAO();
			valor = this.fncDAO.quantidadeSuspensoGerente(this.sessaoMB);
		}
		return valor;
	}

	public int quantidadeVenctoLancto() {
		this.fncDAO = new FinanceiroDAO();
		int valor = this.fncDAO.quantidadeVencimentoLancto();
		return valor;
	}

	public int quantidadeVenctoGerente() {
		this.fncDAO = new FinanceiroDAO();
		int valor = this.fncDAO.quantidadeVencimentoGerente();
		return valor;
	}

	public void salvarSIPSIGAGER() {
		this.cndpagar.setUsuarioAprovacao(null);
		this.fncDAO = new FinanceiroDAO();

		this.cndpagar.setStatusSIP(5);
		this.cndpagar.setVistoGerente(true);
		this.cndpagar.setFeitoGerenteSIP(new Date());
		this.cndpagar.setReprovadoGerente(0);

		this.fncDAO.adicionaLanctoSIP(this.cndpagar, this.sessaoMB, this.obsLancto);

		this.fncDAO.updateSGLTIMP(this.cndpagar);
		this.fncDAO.updateSGIMPOS(this.cndpagar);

		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Salvo!", "Salvo!"));
		RequestContext.getCurrentInstance().update("frmPreLancto:msg0");
		this.listarCndpagarGerente = null;
		this.obsLancto = "";
	}

	public void listarTributos(cndpagar pagar) {
		this.fncDAO = new FinanceiroDAO();
		this.listaDeTributos = null;
		if (this.listaDeTributos == null) {
			this.listaDeTributos = this.fncDAO.listarTributos(pagar);
		}
	}

	public void notificacaoLancamento(cndpagar pagar) {
		this.fncDAO = new FinanceiroDAO();
		this.cndpagar = pagar;
		this.listaDeDuplicidade = this.fncDAO.pesquisarDuplicidade(pagar);
	}

	public void removerSemelhante() {
		this.fncDAO = new FinanceiroDAO();
		this.cndpagar.setSemelhante(false);
		this.fncDAO.removerSemelhanca(this.cndpagar, this.sessaoMB);
		this.msgSalvo();
		this.listarCndpagarGerente = null;
	}

	public void abreProximoLancamentos() {
		try {
			this.fncDAO = new FinanceiroDAO();
			List<cndpagar> l = this.fncDAO.getListaLancamentoContas(this.opcaoLancto,
					this.sessaoMB.getUsuario().getEmail(), this.filtroCndpagarContasInicial,
					this.filtroCndpagarContasFinal);
			cndpagar c = null;
			for (cndpagar aux : l) {
				if (aux.getFeitoFiscalSIP() != null) {
					c = aux;
					break;
				}
			}
			if (c == null) {
				throw new IntranetException("Não existe nenhum lançamento para abrir!");
			} else {
				this.fncDAO.setUsuarioAprovacao(c.getCodigo(), this.sessaoMB.getUsuario().getEmail());
				this.avancar(c, 1);
			}
		} catch (IntranetException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage(), ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void estamparCanceladoPDF() {
		try {
			LancamentoDAO dao = new LancamentoDAO();
			byte[] pdfFinal = PDFUtil.estamparCanceladoPDF(this.imagemSelecionada.getImagem(), this.paginaCancelamento);
			if (this.imagemDesfazer == null) {
				this.imagemDesfazer = this.imagemSelecionada;
			}
			dao.estamparCanceladoPDF(pdfFinal, this.imagemSelecionada.getCodigo());
			if (this.getImagemSelecionada().getId() > 0) {
				this.imagemSelecionada = dao
						.pesquisarImagemPorEtiqueta(Double.valueOf(this.imagemSelecionada.getId()).longValue());
			}
			RequestContext.getCurrentInstance().execute("PF('dlgCarimbar').hide();");
		} catch (IntranetException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage(), ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void desfazerEstampaCanceladoPDF() {
		try {
			LancamentoDAO dao = new LancamentoDAO();
			dao.desfazerPDF(this.imagemDesfazer.getImagem(), this.imagemSelecionada.getCodigo());
			if (this.imagemSelecionada.getId() > 0) {
				long etiqueta = Double.valueOf(this.imagemSelecionada.getId()).longValue();
				this.imagemSelecionada = dao.pesquisarImagemPorEtiqueta(etiqueta);
			}
			this.imagemDesfazer = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fundirPDF(FileUploadEvent event)
			throws IOException, InvalidFormatException, SQLException, java.text.ParseException, ClassNotFoundException {
		byte[] arquivo = event.getFile().getContents();
		try {
			byte[] pdfFinal = PDFUtil.fusaoPDF(arquivo, this.imagemSelecionada.getImagem(), this.pagina);
			if (this.imagemDesfazer == null) {
				this.imagemDesfazer = this.imagemSelecionada;
			}
			LancamentoDAO dao = new LancamentoDAO();
			dao.fundirPDF(pdfFinal, this.imagemSelecionada.getCodigo());
			if (this.imagemSelecionada.getId() > 0) {
				this.imagemSelecionada = dao
						.pesquisarImagemPorEtiqueta(Double.valueOf(this.imagemSelecionada.getId()).longValue());
			}
			RequestContext.getCurrentInstance().execute("PF('dlgFundir').hide();");
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
					"Ocorreu um erro ao importar!Contate o administrador!", ""));
			e.printStackTrace();
		}
	}

	public void desfazerFusaoPDF() {
		try {
			LancamentoDAO dao = new LancamentoDAO();
			dao.desfazerPDF(this.imagemDesfazer.getImagem(), this.imagemSelecionada.getCodigo());
			if (this.imagemSelecionada.getId() > 0) {
				this.imagemSelecionada = dao
						.pesquisarImagemPorEtiqueta(Double.valueOf(this.imagemSelecionada.getId()).longValue());
			}
			this.imagemDesfazer = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void salvarDetalhamento() throws ParseException {
		ControleContasNovoDAO ccnDAO = new ControleContasNovoDAO();
		intra_controle_contas2 icc2 = new intra_controle_contas2();
		this.iccd.setCondominio(this.cndpagar.getCondominio());
		this.iccd.setVencimento(this.cndpagar.getVencimento());
		this.iccd.setCnpj(String.valueOf(this.cndpagar.getCnpj()));
		this.iccd.setNomeFornecedor(this.fornecedorSelecionado.getNome());
		this.setLdValor(this.getLdValor().replace("R$ ", ""));
		this.iccd.setValorLiquido(this.cndpagar.getValor());
		this.iccd.setValorBruto(this.cndpagar.getValorLancto());
		this.iccd.setValorRetencao(this.iccd.getValorBruto() - this.iccd.getValorLiquido());
		if (this.cndpagar.getParcelado().equals("N")) {
			this.iccd.setParcelado(false);
		} else {
			this.iccd.setParcelado(true);
		}
		this.iccd.setConta(this.cndpagar.getConta());
		this.iccd.setHist(this.cndpagar.getHist());
		this.iccd.setHistorico(this.cndpagar.getHistorico());
		this.iccd.setNroEtiqueta(this.imagemSelecionada.getCodigo());
		this.iccd.setNroLancto(this.cndpagar.getNrolancto());

		DateTime data = new DateTime(this.iccd.getVencimento());
		int ano = 0;
		ano = Integer.valueOf(data.getYear());
		List<intra_controle_contas2> listaConta = ccnDAO.listarContas2(this.iccd.getCondominio(), ano);
		if (listaConta == null || listaConta.isEmpty()) {
			icc2.setCondominio(this.iccd.getCondominio());
			icc2.setCnpj(this.iccd.getCnpj());
			icc2.setConta(this.iccd.getConta());
			icc2.setCodigoGerente(this.sessaoMB.getGerenteSelecionado().getCodigo());
			icc2.setAno(ano);
			ccnDAO.salvarConta(icc2);
			this.controleContasNovo(icc2);
		}
		for (intra_controle_contas2 ic : listaConta) {
			if (ic.getCondominio() == this.iccd.getCondominio() && ic.getCnpj() == this.iccd.getCnpj()
					&& ic.getConta() == this.iccd.getConta()) {
				ic = icc2;
				this.controleContasNovo(icc2);
			} else {

			}
		}

		ccnDAO.salvarDetalhamentoPreLancto(this.iccd);
	}

	public void controleContasNovo(intra_controle_contas2 ic) {
		ControleContasNovoDAO ccnDAO = new ControleContasNovoDAO();
		DateTime data = new DateTime(this.iccd.getVencimento());
		int mes = 0;
		double valor = 0;
		mes = Integer.valueOf(data.getMonthOfYear());
		if (mes == 1) {
			valor = ic.getJaneiro() + this.iccd.getValorLiquido();
			ccnDAO.updateJaneiro(ic, valor);
		}
		if (mes == 2) {
			valor = ic.getFevereiro() + this.iccd.getValorLiquido();
			ccnDAO.updateFevereiro(ic, valor);
		}
		if (mes == 3) {
			valor = ic.getMarco() + this.iccd.getValorLiquido();
			ccnDAO.updateMarço(ic, valor);
		}
		if (mes == 4) {
			valor = ic.getAbril() + this.iccd.getValorLiquido();
			ccnDAO.updateAbril(ic, valor);
		}
		if (mes == 5) {
			valor = ic.getMaio() + this.iccd.getValorLiquido();
			ccnDAO.updateMaio(ic, valor);
		}
		if (mes == 6) {
			valor = ic.getJunho() + this.iccd.getValorLiquido();
			ccnDAO.updateJunho(ic, valor);
		}
		if (mes == 7) {
			valor = ic.getJulho() + this.iccd.getValorLiquido();
			ccnDAO.updateJulho(ic, valor);
		}
		if (mes == 8) {
			valor = ic.getAgosto() + this.iccd.getValorLiquido();
			ccnDAO.updateAgosto(ic, valor);
		}
		if (mes == 9) {
			valor = ic.getSetembro() + this.iccd.getValorLiquido();
			ccnDAO.updateSetembro(ic, valor);
		}
		if (mes == 10) {
			valor = ic.getOutubro() + this.iccd.getValorLiquido();
			ccnDAO.updateOutubro(ic, valor);
		}
		if (mes == 11) {
			valor = ic.getNovembro() + this.iccd.getValorLiquido();
			ccnDAO.updateNovembro(ic, valor);
		}
		if (mes == 12) {
			valor = ic.getDezembro() + this.iccd.getValorLiquido();
			ccnDAO.updateDezembro(ic, valor);
		}
	}
}
