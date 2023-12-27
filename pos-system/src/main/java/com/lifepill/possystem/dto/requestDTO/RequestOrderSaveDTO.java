package com.lifepill.possystem.dto.requestDTO;

import com.lifepill.possystem.entity.OrderDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestOrderSaveDTO {

    private int cashiers;
    private Date orderDate;
    private Double total;
    private List<RequestOrderDetailsSaveDTO> orderDetails;
}
