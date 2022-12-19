package store.view.structured;

import kalix.javasdk.view.View;
import kalix.springsdk.annotations.Query;
import kalix.springsdk.annotations.Subscribe;
import kalix.springsdk.annotations.Table;
import kalix.springsdk.annotations.ViewId;
import org.springframework.web.bind.annotation.GetMapping;
import store.customer.api.CustomerEntity;
import store.customer.domain.CustomerEvent;
import store.order.api.OrderEntity;
import store.order.domain.Order;
import store.product.api.ProductEntity;
import store.product.domain.ProductEvent;
import store.view.model.Customer;
import store.view.model.Product;

@ViewId("structured-customer-orders")
public class StructuredCustomerOrdersView {

  // tag::query[]
  @GetMapping("/structured-customer-orders/{customerId}")
  @Query( // <1>
      """
      SELECT
       customers.customerId AS id,
       (name,
        address.street AS address1,
        address.city AS address2,
        email AS contactEmail) AS shipping,
       (products.productId AS id,
        productName AS name,
        quantity,
        (price.currency, price.units, price.cents) AS value,
        orderId,
        createdTimestamp AS orderCreatedTimestamp) AS orders
      FROM customers
      JOIN orders ON orders.customerId = customers.customerId
      JOIN products ON products.productId = orders.productId
      WHERE customers.customerId = :customerId
      ORDER BY orders.createdTimestamp
      """)
  public CustomerOrders get(String customerId) {
    return null;
  }
  // end::query[]

  @Table("customers")
  @Subscribe.EventSourcedEntity(CustomerEntity.class)
  public static class Customers extends View<Customer> {
    public UpdateEffect<Customer> onEvent(CustomerEvent.CustomerCreated created) {
      String id = updateContext().eventSubject().orElse("");
      return effects()
          .updateState(new Customer(id, created.email(), created.name(), created.address()));
    }

    public UpdateEffect<Customer> onEvent(CustomerEvent.CustomerNameChanged event) {
      return effects().updateState(viewState().withName(event.newName()));
    }

    public UpdateEffect<Customer> onEvent(CustomerEvent.CustomerAddressChanged event) {
      return effects().updateState(viewState().withAddress(event.newAddress()));
    }
  }

  @Table("products")
  @Subscribe.EventSourcedEntity(ProductEntity.class)
  public static class Products extends View<Product> {
    public UpdateEffect<Product> onEvent(ProductEvent.ProductCreated created) {
      String id = updateContext().eventSubject().orElse("");
      return effects().updateState(new Product(id, created.name(), created.price()));
    }

    public UpdateEffect<Product> onEvent(ProductEvent.ProductNameChanged event) {
      return effects().updateState(viewState().withProductName(event.newName()));
    }

    public UpdateEffect<Product> onEvent(ProductEvent.ProductPriceChanged event) {
      return effects().updateState(viewState().withPrice(event.newPrice()));
    }
  }

  @Table("orders")
  @Subscribe.ValueEntity(OrderEntity.class)
  public static class Orders extends View<Order> {}
}
