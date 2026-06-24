package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.bien.CategoriaBien;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDonacion {
    private LocalDate fecha;
    private CategoriaBien categoria;
}
