import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.com.alura.leilao.dao.PagamentoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Pagamento;
import br.com.alura.leilao.model.Usuario;
import br.com.alura.leilao.service.GeradorDePagamento;

public class GeradorDePagamentoTest {
  @Mock
  private PagamentoDao pagamentoDaoMock;
  @Mock
  private Clock clock;
  @Captor
  private ArgumentCaptor<Pagamento> captor;
  private GeradorDePagamento sut;

  @Before
  public void setup() {
    // pagamentoDao = Mockito.mock(PagamentoDao.class);
    // captor = ArgumentCaptor.forClass(Pagamento.class);
    MockitoAnnotations.initMocks(this);
    this.sut = new GeradorDePagamento(pagamentoDaoMock, clock);
  }

  private Leilao pegaLeilao() {
    Leilao leilao = new Leilao("Celular", new BigDecimal("500"), new Usuario("Fulano"));
    leilao.propoe(new Lance(new Usuario("Beltrano"), new BigDecimal("900")));
    return leilao;
  }

  public LocalDate mockAndGetDate(int year, int month, int day) {
    LocalDate date = LocalDate.of(year, month, day);
    Instant instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Mockito.when(clock.instant()).thenReturn(instant);
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    return date;
  } 

  @Test
  public void shoudGeneratePaymentWhenIsBusinessDay() {
    Leilao leilao = pegaLeilao();
    Lance lanceVencedor = leilao.getLances().get(0);
    LocalDate date = mockAndGetDate(2020, 12, 7);

    sut.gerarPagamento(lanceVencedor);

    verify(pagamentoDaoMock).salvar(captor.capture());
    Pagamento pagamento = captor.getValue();
    assertEquals(date.plusDays(1), pagamento.getVencimento());
    assertEquals(lanceVencedor.getValor(), pagamento.getValor());
    assertFalse(pagamento.getPago());
    assertEquals(lanceVencedor.getUsuario(), pagamento.getUsuario());
    assertEquals(leilao, pagamento.getLeilao());
  }

  @Test
  public void shouldSkip2DaysWhenIsSaturday() {
    Leilao leilao = pegaLeilao();
    Lance lanceVencedor = leilao.getLances().get(0);
    LocalDate date = mockAndGetDate(2020, 12, 5);

    sut.gerarPagamento(lanceVencedor);

    verify(pagamentoDaoMock).salvar(captor.capture());
    Pagamento pagamento = captor.getValue();
    assertEquals(date.plusDays(2), pagamento.getVencimento());
  }
}
