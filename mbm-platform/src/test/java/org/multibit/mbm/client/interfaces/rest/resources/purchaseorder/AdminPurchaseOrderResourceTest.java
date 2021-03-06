package org.multibit.mbm.client.interfaces.rest.resources.purchaseorder;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.xeiam.xchange.currency.MoneyUtils;
import org.joda.money.BigMoney;
import org.junit.Test;
import org.multibit.mbm.client.common.pagination.PaginatedArrayList;
import org.multibit.mbm.client.common.pagination.PaginatedLists;
import org.multibit.mbm.client.domain.model.model.*;
import org.multibit.mbm.client.domain.repositories.ItemReadService;
import org.multibit.mbm.client.domain.repositories.PurchaseOrderReadService;
import org.multibit.mbm.client.infrastructure.persistence.DatabaseLoader;
import org.multibit.mbm.client.interfaces.rest.api.cart.purchaseorder.BuyerPurchaseOrderItem;
import org.multibit.mbm.client.interfaces.rest.api.cart.purchaseorder.BuyerUpdatePurchaseOrderRequest;
import org.multibit.mbm.client.interfaces.rest.api.hal.HalMediaType;
import org.multibit.mbm.testing.BaseJerseyHmacResourceTest;
import org.multibit.mbm.testing.FixtureAsserts;

import javax.ws.rs.core.MediaType;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdminPurchaseOrderResourceTest extends BaseJerseyHmacResourceTest {

  private final PurchaseOrderReadService purchaseOrderReadService = mock(PurchaseOrderReadService.class);
  private final ItemReadService itemReadService = mock(ItemReadService.class);

  private final AdminPurchaseOrderResource testObject = new AdminPurchaseOrderResource();

  @Override
  protected void setUpResources() {

    // Create the User for authenticated access
    User adminUser = setUpTrentHmacAuthenticator();
    adminUser.setId(1L);

    // Create the supporting Role
    Role supplierRole = DatabaseLoader.buildSupplierRole();

    // Configure Steve Supplier
    User steveUser = DatabaseLoader.buildSteveSupplier(supplierRole);
    steveUser.setId(1L);
    steveUser.getSupplier().setId(1L);

    // Configure Steve's PurchaseOrder with Items
    Item book1 = DatabaseLoader.buildBookItemCryptonomicon();
    book1.setId(1L);
    Item book2 = DatabaseLoader.buildBookItemQuantumThief();
    book2.setId(2L);

    // TODO Pull this into DatabaseLoader
    BigMoney book1UnitPrice = MoneyUtils.parse("GBP 1.23");
    BigMoney book1UnitTax = MoneyUtils.parse("GBP 0.20");
    BigMoney book2UnitPrice = MoneyUtils.parse("GBP 2.46");
    BigMoney book2UnitTax = MoneyUtils.parse("GBP 0.40");

    PurchaseOrder stevePurchaseOrder1 = PurchaseOrderBuilder
      .newInstance()
      .withSupplier(steveUser.getSupplier())
      .withPurchaseOrderItem(book1, 1, book1UnitPrice, book1UnitTax)
      .withPurchaseOrderItem(book2, 2, book2UnitPrice, book2UnitTax)
      .build();
    stevePurchaseOrder1.setId(1L);
    steveUser.getSupplier().getPurchaseOrders().add(stevePurchaseOrder1);

    // Configure Sam Supplier
    User samUser = DatabaseLoader.buildSamSupplier(supplierRole);
    samUser.setId(1L);
    samUser.getSupplier().setId(1L);

    // Configure Sam's PurchaseOrder with Items
    Item book3 = DatabaseLoader.buildBookItemCompleteWorks();
    book3.setId(3L);
    Item book4 = DatabaseLoader.buildBookItemPlumbing();
    book4.setId(4L);

    // TODO Pull this into DatabaseLoader
    BigMoney book3UnitPrice = MoneyUtils.parse("GBP 1.23");
    BigMoney book3UnitTax = MoneyUtils.parse("GBP 0.20");
    BigMoney book4UnitPrice = MoneyUtils.parse("GBP 2.46");
    BigMoney book4UnitTax = MoneyUtils.parse("GBP 0.40");

    PurchaseOrder samPurchaseOrder1 = PurchaseOrderBuilder
      .newInstance()
      .withSupplier(samUser.getSupplier())
      .withPurchaseOrderItem(book3, 3, book3UnitPrice, book3UnitTax)
      .withPurchaseOrderItem(book4, 4, book4UnitPrice, book4UnitTax)
      .build();
    samPurchaseOrder1.setId(1L);
    samUser.getSupplier().getPurchaseOrders().add(samPurchaseOrder1);

    // Create some mock results
    Set<PurchaseOrder> stevePurchaseOrders = Sets.newHashSet(steveUser.getSupplier().getPurchaseOrders());
    Set<PurchaseOrder> samPurchaseOrders = Sets.newHashSet(samUser.getSupplier().getPurchaseOrders());

    // Configure PurchaseOrder read service
    PaginatedArrayList<PurchaseOrder> stevesPurchaseOrders = PaginatedLists.newPaginatedArrayList(1, 2,1, Lists.newArrayList(stevePurchaseOrders));
    PaginatedArrayList<PurchaseOrder> samsPurchaseOrders = PaginatedLists.newPaginatedArrayList(1,2,1, Lists.newArrayList(samPurchaseOrders));
    when(purchaseOrderReadService.getById(stevePurchaseOrder1.getId())).thenReturn(Optional.of(stevePurchaseOrder1));
    when(purchaseOrderReadService.getById(samPurchaseOrder1.getId())).thenReturn(Optional.of(samPurchaseOrder1));
    when(purchaseOrderReadService.getPaginatedList(1, 0)).thenReturn(stevesPurchaseOrders);
    when(purchaseOrderReadService.getPaginatedList(1, 1)).thenReturn(samsPurchaseOrders);
    when(purchaseOrderReadService.saveOrUpdate(stevePurchaseOrder1)).thenReturn(stevePurchaseOrder1);
    when(purchaseOrderReadService.saveOrUpdate(samPurchaseOrder1)).thenReturn(samPurchaseOrder1);

    // Configure Item read service
    when(itemReadService.getBySKU(book1.getSKU())).thenReturn(Optional.of(book1));
    when(itemReadService.getBySKU(book2.getSKU())).thenReturn(Optional.of(book2));
    when(itemReadService.getBySKU(book3.getSKU())).thenReturn(Optional.of(book3));
    when(itemReadService.getBySKU(book4.getSKU())).thenReturn(Optional.of(book4));

    testObject.setPurchaseOrderReadService(purchaseOrderReadService);
    testObject.setItemReadService(itemReadService);

    // Configure resources
    addSingleton(testObject);

  }

  @Test
  public void adminRetrievePurchaseOrdersAsHalJson() throws Exception {

    String actualResponse = configureAsClient(AdminPurchaseOrderResource.class)
      .queryParam("ps", "1")
      .queryParam("pn", "0")
      .accept(HalMediaType.APPLICATION_HAL_JSON)
      .get(String.class);

    FixtureAsserts.assertStringMatchesJsonFixture("PurchaseOrder list 1 can be retrieved as HAL+JSON", actualResponse, "/fixtures/hal/purchaseorder/expected-admin-retrieve-purchase-orders-page-1.json");

    actualResponse = configureAsClient(AdminPurchaseOrderResource.class)
      .queryParam("ps", "1")
      .queryParam("pn", "1")
      .accept(HalMediaType.APPLICATION_HAL_JSON)
      .get(String.class);

    FixtureAsserts.assertStringMatchesJsonFixture("PurchaseOrder list 2 can be retrieved as HAL+JSON", actualResponse, "/fixtures/hal/purchaseorder/expected-admin-retrieve-purchase-orders-page-2.json");

  }

  @Test
  public void adminUpdatePurchaseOrderAsHalJson() throws Exception {

    // Starting condition is Belinda requires {book1: 1, book2: 2}
    // Ending condition is Belinda requires {book1: 0, book2: 2, book3: 3}

    BuyerUpdatePurchaseOrderRequest updatePurchaseOrderRequest = new BuyerUpdatePurchaseOrderRequest();
    // TODO Consider providing ID (sequences etc)
    //updatePurchaseOrderRequest.setId(1L);
    // Add a few new items
    updatePurchaseOrderRequest.getPurchaseOrderItems().add(new BuyerPurchaseOrderItem("0316184136", 3, "1.1", "GBP"));
    // Remove by setting to zero
    updatePurchaseOrderRequest.getPurchaseOrderItems().add(new BuyerPurchaseOrderItem("0099410672", 0, "2.2", "GBP"));

    String actualResponse = configureAsClient("/admin/purchase-orders/1")
      .accept(HalMediaType.APPLICATION_HAL_JSON)
      .entity(updatePurchaseOrderRequest, MediaType.APPLICATION_JSON_TYPE)
      .put(String.class);

    FixtureAsserts.assertStringMatchesJsonFixture("UpdatePurchaseOrder by admin response render to HAL+JSON", actualResponse, "/fixtures/hal/purchaseorder/expected-admin-update-purchase-order.json");

  }

}
