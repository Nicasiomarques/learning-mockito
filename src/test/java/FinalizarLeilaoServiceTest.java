import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Usuario;
import br.com.alura.leilao.service.EnviadorDeEmails;
import br.com.alura.leilao.service.FinalizarLeilaoService;

public class FinalizarLeilaoServiceTest {
  @Mock
  private LeilaoDao leilaoDaoMock;
  @Mock
  private EnviadorDeEmails enviadorDeEmailsMock;
  private FinalizarLeilaoService sut;

  @Before
  public void beforeEach() {
    MockitoAnnotations.initMocks(this);
    this.sut = new FinalizarLeilaoService(leilaoDaoMock, enviadorDeEmailsMock);
  }

  private static Lance lanceFactory(String name, String valor) {
    return new Lance(new Usuario(name), new BigDecimal(valor));
  }

  private ArrayList<Leilao> pegaLeiloesExpirados() {
    ArrayList<Leilao> leiloes = new ArrayList<>();
    Leilao leilao = new Leilao("Celular", new BigDecimal("500"), new Usuario("Fulano"));
    leilao.propoe(lanceFactory("Cicrano", "600"));
    leilao.propoe(lanceFactory("Beltrano", "900"));
    leiloes.add(leilao);
    return leiloes;
  }

  @Test
  public void ShouldEndAuction() {
    ArrayList<Leilao> leiloes = pegaLeiloesExpirados();
    when(leilaoDaoMock.buscarLeiloesExpirados()).thenReturn(leiloes);

    sut.finalizarLeiloesExpirados();
    
    Leilao leilao = leiloes.get(0);
    assertTrue(leilao.isFechado());
    assertEquals(new BigDecimal("900"), leilao.getLanceVencedor().getValor());
    assertEquals("Beltrano", leilao.getLanceVencedor().getUsuario().getNome());
    verify(leilaoDaoMock).salvar(leilao);
  }


  @Test
  public void ensureEmailWillSendedToAuctionWinner() {
    ArrayList<Leilao> leiloes = pegaLeiloesExpirados();
    when(leilaoDaoMock.buscarLeiloesExpirados()).thenReturn(leiloes);

    sut.finalizarLeiloesExpirados();
    
    Leilao leilao = leiloes.get(0);
    verify(enviadorDeEmailsMock).enviarEmailVencedorLeilao(leilao.getLanceVencedor());
  }
}
