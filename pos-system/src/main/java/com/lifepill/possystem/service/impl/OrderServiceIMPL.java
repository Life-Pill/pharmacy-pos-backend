package com.lifepill.possystem.service.impl;

import com.lifepill.possystem.dto.requestDTO.RequestOrderDetailsSaveDTO;
import com.lifepill.possystem.dto.requestDTO.RequestOrderSaveDTO;
import com.lifepill.possystem.dto.requestDTO.RequestPaymentDetailsDTO;
import com.lifepill.possystem.entity.Item;
import com.lifepill.possystem.entity.Order;
import com.lifepill.possystem.entity.OrderDetails;
import com.lifepill.possystem.entity.PaymentDetails;
import com.lifepill.possystem.exception.InsufficientItemQuantityException;
import com.lifepill.possystem.exception.NotFoundException;
import com.lifepill.possystem.repo.branchRepository.BranchRepository;
import com.lifepill.possystem.repo.employerRepository.EmployerRepository;
import com.lifepill.possystem.repo.itemRepository.ItemRepository;
import com.lifepill.possystem.repo.orderRepository.OrderDetailsRepository;
import com.lifepill.possystem.repo.orderRepository.OrderRepository;
import com.lifepill.possystem.repo.paymentRepository.PaymentRepository;
import com.lifepill.possystem.service.OrderService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceIMPL implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EmployerRepository employerRepository;
    @Autowired
    private OrderDetailsRepository orderDetailsRepo;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private PaymentRepository paymentRepository;


    /**
     * Adds an order to the system.
     * @param requestOrderSaveDTO The DTO containing order details.
     * @return A message indicating the result of the operation.
     */
    @Override
    public String addOrder(RequestOrderSaveDTO requestOrderSaveDTO) {
        // Check if items in the order have sufficient quantity
        checkItemStock(requestOrderSaveDTO);

        // Update item quantities
        updateItemQuantities(requestOrderSaveDTO);

        Order order = new Order();
        order.setEmployer(employerRepository.getById(requestOrderSaveDTO.getEmployerId()));
        order.setOrderDate(requestOrderSaveDTO.getOrderDate());
        order.setTotal(requestOrderSaveDTO.getTotal());
        order.setBranchId(requestOrderSaveDTO.getBranchId());
        orderRepository.save(order);

        if (orderRepository.existsById(order.getOrderId())){

            List<OrderDetails> orderDetails = modelMapper.
                    map(requestOrderSaveDTO.getOrderDetails(),new TypeToken<List<OrderDetails>>(){
                    }.getType());

            for (int i=0;i<orderDetails.size();i++){
                orderDetails.get(i).setOrders(order);
                orderDetails.get(i).setItems(itemRepository.getById(requestOrderSaveDTO.getOrderDetails().get(i).getId()));
            }

            if (orderDetails.size()>0){
                orderDetailsRepo.saveAll(orderDetails);
            }

            savePaymentDetails(requestOrderSaveDTO.getPaymentDetails().get(0), order);
            return "saved";
        }
        return "Order saved successfully";
    }

    private void savePaymentDetails(RequestPaymentDetailsDTO paymentDetailsDTO, Order order) {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setPaymentMethod(paymentDetailsDTO.getPaymentMethod());
        paymentDetails.setPaymentAmount(paymentDetailsDTO.getPaymentAmount());
        paymentDetails.setPaymentDate(paymentDetailsDTO.getPaymentDate());
        paymentDetails.setPaymentNotes(paymentDetailsDTO.getPaymentNotes());
        paymentDetails.setPaymentDiscount(paymentDetailsDTO.getPaymentDiscount());
        paymentDetails.setPayedAmount(paymentDetailsDTO.getPayedAmount());
        paymentDetails.setOrders(order); // Set the order for which this payment is made
        paymentRepository.save(paymentDetails);
    }

    /*private void checkItemStock(RequestOrderSaveDTO requestOrderSaveDTO) {
        for (RequestOrderDetailsSaveDTO orderDetail : requestOrderSaveDTO.getOrderDetails()) {
            Optional<Item> optionalItem = itemRepo.findById(orderDetail.getItems());
            if (optionalItem.isPresent()) {
                Item item = optionalItem.get();
                if (item.getItemQuantity() < orderDetail.getQty()) {
                    throw new InsufficientItemQuantityException("Item " + item.getItemId() + " does not have enough quantity");
                }
            } else {
                throw new NotFoundException("Item not found with ID: " + orderDetail.getItems());
            }
        }
    }*/

    private void checkItemStock(RequestOrderSaveDTO requestOrderSaveDTO) {
        for (RequestOrderDetailsSaveDTO orderDetail : requestOrderSaveDTO.getOrderDetails()) {
            Optional<Item> optionalItem = itemRepository.findById(orderDetail.getId());
            if (optionalItem.isPresent()) {
                Item item = optionalItem.get();
                if (item.getItemQuantity() < orderDetail.getQty()) {
                    throw new InsufficientItemQuantityException("Item " + item.getItemId() + " does not have enough quantity");
                }
            } else {
                throw new NotFoundException("Item not found with ID: " + orderDetail.getId());
            }
        }
    }

    private void updateItemQuantities(RequestOrderSaveDTO requestOrderSaveDTO) {
        for (RequestOrderDetailsSaveDTO orderDetail : requestOrderSaveDTO.getOrderDetails()) {
            Optional<Item> optionalItem = itemRepository.findById(orderDetail.getId());
            if (optionalItem.isPresent()) {
                Item item = optionalItem.get();
                int remainingQuantity = (int) (item.getItemQuantity() - orderDetail.getQty());
                item.setItemQuantity(remainingQuantity);
                itemRepository.save(item);
            } else {
                throw new NotFoundException("Item not found with ID: " + orderDetail.getId());
            }
        }
    }
}
