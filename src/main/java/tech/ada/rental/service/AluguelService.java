package tech.ada.rental.service;

import tech.ada.rental.model.Aluguel;
import tech.ada.rental.repository.AluguelRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class AluguelService {

    AluguelRepository repository;

    public AluguelService(AluguelRepository repository) {
        this.repository = repository;
    }

    public Aluguel criarAluguel(Aluguel aluguel) {
        if (aluguel.getVeiculo().isDisponibilidade()) {
            aluguel.getVeiculo().setDisponibilidade(false);
            return repository.save(aluguel);
        }

        throw new RuntimeException("Veiculo indisponível");
    }

    public Aluguel devolverVeiculo(Aluguel aluguel) {
        aluguel.getVeiculo().setDisponibilidade(true);
        aluguel.setDevolucao(LocalDateTime.now());
        aluguel.setPrecoAluguel(calcularAluguel(aluguel));
        return repository.save(aluguel);
    }

    private BigDecimal calcularAluguel(Aluguel aluguel) {
        aluguel.setDevolucao(LocalDateTime.now());

        Duration duration = Duration.between(aluguel.getInicioAluguel(), aluguel.getDevolucao());

        if (duration.toHours() % 24 != 0) {
            duration = duration.plusDays(1);
        }

        long dias = duration.toDays();

        aluguel.setDiarias(dias);

        BigDecimal desconto = aluguel.getCliente().getTipo().calculoDesconto(aluguel);
        BigDecimal valorDiaria = aluguel.getVeiculo().getValorDiaria();

        BigDecimal valorTotal = valorDiaria.multiply(BigDecimal.valueOf(dias)).multiply(desconto);
        aluguel.setPrecoAluguel(valorTotal);

        return valorTotal;
    }

    public Aluguel buscarPorId(long l) {
        return repository.findById(l);
    }
}
