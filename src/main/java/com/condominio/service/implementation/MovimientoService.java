package com.condominio.service.implementation;

import com.condominio.dto.request.MovimientoRequestDTO;
import com.condominio.dto.response.MetricasDTO;
import com.condominio.dto.response.MovimientoDTO;
import com.condominio.dto.response.MovimientosMesDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Movimiento;
import com.condominio.persistence.model.TipoMovimiento;
import com.condominio.persistence.repository.MovimientoRepository;
import com.condominio.service.interfaces.IMovimientoService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoService implements IMovimientoService {

    private final MovimientoRepository repo;
    private final ModelMapper modelMapper;

    @Override
    public SuccessResult<MovimientosMesDTO> getMovimientosPorMes(int mes, int anio) {
        YearMonth ym = YearMonth.of(anio, mes);
        LocalDate desde = ym.atDay(1);
        LocalDate hasta = ym.atEndOfMonth();

        List<Movimiento> movimientos = repo.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(desde, hasta);

        int ingresos = repo.sumMontoByTipoBetween(TipoMovimiento.ENTRADA, desde, hasta);
        int egresos = repo.sumMontoByTipoBetween(TipoMovimiento.SALIDA, desde, hasta);
        int balance = ingresos - egresos;

        int ingresosAcumulados = repo.sumMontoByTipoBetween(TipoMovimiento.ENTRADA, LocalDate.of(1900,1,1), hasta);
        int egresosAcumulados = repo.sumMontoByTipoBetween(TipoMovimiento.SALIDA, LocalDate.of(1900,1,1), hasta);
        int saldo = ingresosAcumulados - egresosAcumulados;

        List<MovimientoDTO> dtos = movimientos.stream()
                .map(movimiento -> modelMapper.map(movimiento, MovimientoDTO.class))
                .collect(Collectors.toList());

        MetricasDTO metricas = new MetricasDTO(ingresos, egresos, balance, saldo);
        return new SuccessResult<>("Movimientos obtenidos exitosamente", new MovimientosMesDTO(dtos, metricas));
    }

    @Override
    public SuccessResult<MovimientoDTO> crearMovimiento(MovimientoRequestDTO req) {
        Movimiento movimiento = Movimiento.builder()
                .fechaMovimiento(req.getFecha() == null ? LocalDate.now() : req.getFecha())
                .tipoMovimiento(req.getTipo())
                .categoriaMovimiento(req.getCategoria())
                .descripcion(req.getDescripcion())
                .monto(req.getMonto())
                .responsable(req.getResponsable())
                .build();

        Movimiento saved = repo.save(movimiento);
        return  new SuccessResult<>("Movimiento creado exitosamente", modelMapper.map(saved, MovimientoDTO.class));
    }

    @Override
    public SuccessResult<MovimientoDTO> actualizarMovimiento(Long id, MovimientoRequestDTO req) {
        Movimiento movimiento = repo.findById(id)
                .orElseThrow(() -> new ApiException("Movimiento no encontrado con id " + id, HttpStatus.NOT_FOUND));
        movimiento.setFechaMovimiento(req.getFecha());
        movimiento.setTipoMovimiento(req.getTipo());
        movimiento.setDescripcion(req.getDescripcion());
        movimiento.setMonto(req.getMonto());
        movimiento.setCategoriaMovimiento(req.getCategoria());
        movimiento.setResponsable((req.getResponsable() == null || req.getResponsable().isBlank()) ? null : req.getResponsable());
        Movimiento updated = repo.save(movimiento);

        return new SuccessResult<>("Movimiento actualizado exitosamente", modelMapper.map(updated, MovimientoDTO.class)) ;
    }

    @Override
    public SuccessResult<Void> eliminarMovimiento(Long id){
        if (!repo.existsById(id)) {
            throw new ApiException("Movimiento no encontrado con id " + id, HttpStatus.NOT_FOUND);
        }
        repo.deleteById(id);
        return new SuccessResult<>("Movimiento eliminado exitosamente", null);
    }

}
