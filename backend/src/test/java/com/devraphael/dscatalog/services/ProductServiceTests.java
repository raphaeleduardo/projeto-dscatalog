package com.devraphael.dscatalog.services;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devraphael.dscatalog.dto.ProductDTO;
import com.devraphael.dscatalog.entities.Category;
import com.devraphael.dscatalog.entities.Product;
import com.devraphael.dscatalog.repositories.CategoryRepository;
import com.devraphael.dscatalog.repositories.ProductRepository;
import com.devraphael.dscatalog.services.exceptions.DatabaseException;
import com.devraphael.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devraphael.dscatalog.tests.Factory;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;

	@Mock
	private ProductRepository repository;

	@Mock
	private CategoryRepository categoryRepository;

	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private PageImpl<Product> page;
	private Product product;
	private Category category;
	ProductDTO productDto;

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 1000L;
		dependentId = 3L;
		product = Factory.createProduct();
		category = Factory.createCategory();
		productDto = Factory.createProductDTO();
		page = new PageImpl<>(List.of(product));

		Mockito.when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

		Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);

		Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
		Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		Mockito.doNothing().when(repository).deleteById(existingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);

		Mockito.when(repository.getReferenceById(existingId)).thenReturn(product);
		Mockito.when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(categoryRepository.getReferenceById(existingId)).thenReturn(category);
		Mockito.when(categoryRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(repository.existsById(existingId)).thenReturn(true);
		Mockito.when(repository.existsById(nonExistingId)).thenReturn(false);
		Mockito.when(repository.existsById(dependentId)).thenReturn(true);
	}

	@Test
	public void findAllPagedShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);

		Page<ProductDTO> result = service.findAllPaged(pageable);

		Assertions.assertNotNull(result);
		Mockito.verify(repository).findAll(pageable);
	}

	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
	}

	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
	}

	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
	}

	@Test
	public void findByIdShouldReturnProductDTOWhenExistId() {
		ProductDTO result = service.findById(existingId);
		Assertions.assertNotNull(result);
	}

	@Test
	public void findByIdShouldThrowResourceNotFoundExcWhenDoesNotExistId() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
	}

	@Test
	public void updateShouldReturnProductDTOWhenExistId() {
		ProductDTO result = service.update(existingId, productDto);
		Assertions.assertNotNull(result);
	}

	@Test
	public void updateShouldThrowResourceNotFoundExcWhenDoesNotExistId() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, productDto);
		});
	}

}
