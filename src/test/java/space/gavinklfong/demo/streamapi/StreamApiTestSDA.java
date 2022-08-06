package space.gavinklfong.demo.streamapi;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import space.gavinklfong.demo.streamapi.models.Customer;
import space.gavinklfong.demo.streamapi.models.Order;
import space.gavinklfong.demo.streamapi.models.Product;
import space.gavinklfong.demo.streamapi.repos.CustomerRepo;
import space.gavinklfong.demo.streamapi.repos.OrderRepo;
import space.gavinklfong.demo.streamapi.repos.ProductRepo;

@Slf4j
@DataJpaTest
public class StreamApiTestSDA {

	@Autowired
	private CustomerRepo customerRepo;

	@Autowired
	private OrderRepo orderRepo;

	@Autowired
	private ProductRepo productRepo;

	@Test
	public void testFilter() {
		List<Order> delivered = orderRepo.findAll().stream()
				.filter(o -> o.getStatus().equalsIgnoreCase("DELIVERED"))
				.collect(Collectors.toList());

		/*nie przypisywać nulla do kolekcji*/
		List<Order> list = null;

		printData(delivered);


	}
	@Test
	public void testFilterBetter() {
		/*
		NEW
		PENDING
		DELIVERED
		CANCELLED
		ARCHIVED
		* */
		List<String> ALREADY_DONE_STATUSES  = Arrays.asList("DELIVERED","CANCELLED","ARCHIVED");
		List<Order> delivered = filterOrdersByPredicate(order -> !order.getStatus().equals("DELIVERED"));
		List<Order> pending = filterOrdersByPredicate(order -> order.getStatus().equals("PENDING"));
		filterOrdersByPredicate(order -> ALREADY_DONE_STATUSES.contains(order.getStatus()));


		printData(delivered);


	}

	private List<Order> filterOrdersByPredicate(Predicate<Order> condition) {
		return orderRepo.findAll().stream()
				.filter(condition)
				.collect(Collectors.toList());
	}

	@Test
	public void distinctCustomer(){
		List<Customer> customers = orderRepo.findAll().stream()
				.filter(o -> o.getStatus().equalsIgnoreCase("DELIVERED"))
				.map(o -> o.getCustomer())
				//tutaj tylko klienci
				//Klienci mogą się powtarzać, distinct upewni się ze będziemy mieli każdego tylko raz
				.distinct()
				.collect(Collectors.toList());

		printData(customers);

	}
	@Test
	public void sumProductPrices(){
		double pending = orderRepo.findAll().stream()
				.filter(o -> o.getStatus().equals("PENDING"))
				.flatMap(o -> o.getProducts().stream())
				.map(product -> product.getPrice())
				.mapToDouble(value -> value.doubleValue())
				.sum();

		System.out.println(pending);

	}
	@Test
	public void comparingExample(){
		Product product = orderRepo.findAll().stream()
				.flatMap(order -> order.getProducts().stream())
				//.max
				.min(Comparator.comparing(p -> p.getPrice()))
				.orElse(null);

		System.out.println(product);

	}
	@Test
	public void mapExample(){
		Map<String, Set<Product>> customersAndOrders = orderRepo.findAll().stream()
				/*3 parametr, funkcja mergeująca bez tego może się wywalić gdy klucz wystąpi 2x*/
				.collect(Collectors.toMap(o -> o.getCustomer().getName(), o -> o.getProducts(),(firstOccurrance, secondOccurrence) -> firstOccurrance));

		System.out.println(customersAndOrders);

	}
	private static void printData(List<?> orders){
		orders.forEach(System.out::println);
	}


}
