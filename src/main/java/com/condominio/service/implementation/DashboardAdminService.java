package com.condominio.service.implementation;

import com.condominio.dto.response.*;
import com.condominio.persistence.model.Movimiento;
import com.condominio.persistence.model.Pago;
import com.condominio.persistence.model.TipoMovimiento;
import com.condominio.persistence.repository.MovimientoRepository;
import com.condominio.persistence.repository.PagoRepository;
import com.condominio.service.interfaces.IDashboardAdminService;
import com.condominio.service.interfaces.IMovimientoService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardAdminService implements IDashboardAdminService {

    private final MovimientoRepository movimientoRepository;
    private final IMovimientoService movimientoService;

    @Override
    public SuccessResult<ResumenFinancieroDTO> getResumenFinancieronByYear(int year) {

        List<Movimiento> movimientos = movimientoRepository.findByYear(year);
        if (movimientos.isEmpty()) {
            throw new ApiException("No hay movimientos para este año", HttpStatus.OK);
        }

        // Agrupar por número de mes según fechaMovimiento
        Map<Integer, List<Movimiento>> movimientosPorMes = movimientos.stream()
                .collect(Collectors.groupingBy(m -> m.getFechaMovimiento().getMonthValue()));

        // Construir la lista de DTO de meses
        List<ResumenFinancieroMesDTO> meses = movimientosPorMes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Ordenar Ene → Dic
                .map(entry -> {

                    int mesNumero = entry.getKey();
                    List<Movimiento> listaMov = entry.getValue();

                    int entradas = listaMov.stream()
                            .filter(m -> m.getTipoMovimiento() == TipoMovimiento.ENTRADA)
                            .mapToInt(Movimiento::getMonto)
                            .sum();

                    int salidas = listaMov.stream()
                            .filter(m -> m.getTipoMovimiento() == TipoMovimiento.SALIDA)
                            .mapToInt(Movimiento::getMonto)
                            .sum();

                    return ResumenFinancieroMesDTO.builder()
                            .mes(convertirMes(mesNumero))
                            .entradas(entradas)
                            .salidas(salidas)
                            .build();
                })
                .toList();

        return new SuccessResult<>("Información obtenida", ResumenFinancieroDTO.builder()
                .year(year)
                .meses(meses)
                .build());
    }

    @Override
    public SuccessResult<MovimientosMesDTO> getResumenFinancieronMesReciente() {
        return movimientoService.getMovimientosPorMes(LocalDate.now().getMonthValue(), LocalDate.now().getYear());
    }
    private String convertirMes(int mes) {
        return switch (mes) {
            case 1 -> "Ene";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Abr";
            case 5 -> "May";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Ago";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dic";
            default -> "";
        };
    }


}
