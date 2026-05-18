INSERT INTO tickets_estadia (nro_ticket, patente, tipo_vehiculo, hora_entrada, estado, creado_en)
VALUES ('T-2025-000001','ABC123','AUTO','2025-10-27 08:15:00','ABIERTO', NOW());

INSERT INTO tickets_estadia (nro_ticket, patente, tipo_vehiculo, hora_entrada, estado, creado_en)
VALUES ('T-2025-000002','AA123BB','MOTO','2025-10-27 07:50:00','CERRADO', NOW());

UPDATE tickets_estadia
SET hora_salida='2025-10-27 08:40:00', minutos_estadia=50, tarifa_aplicada='MOTO_STD_2025Q4',
    importe_calculado=350.00, forma_pago='EFECTIVO', estado='CERRADO', cerrado_en='2025-10-27 08:40:00'
WHERE nro_ticket='T-2025-000002';