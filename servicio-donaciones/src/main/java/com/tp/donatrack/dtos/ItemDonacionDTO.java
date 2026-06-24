package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.bien.CategoriaBien;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDonacionDTO {
    private LocalDate fecha;
    private CategoriaBien categoria;
}
