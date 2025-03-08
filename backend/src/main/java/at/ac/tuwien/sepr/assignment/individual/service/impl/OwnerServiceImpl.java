package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.mapper.OwnerMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.OwnerDao;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service implementation for managing owner-related operations.
 */
@Service
public class OwnerServiceImpl implements OwnerService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final OwnerDao dao;
  private final OwnerMapper mapper;

  public OwnerServiceImpl(
      OwnerDao dao,
      OwnerMapper mapper) {
    this.dao = dao;
    this.mapper = mapper;
  }

  /**
   * Retrieves an owner by ID.
   *
   * @param id the ID of the owner
   * @return the {@link OwnerDto} representing the owner
   * @throws NotFoundException if the owner is not found
   */
  @Override
  public OwnerDto getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    return mapper.entityToDto(dao.getById(id));
  }

  /**
   * Retrieves multiple owners by their IDs.
   *
   * @param ids the collection of owner IDs to retrieve
   * @return a map of owner IDs to {@link OwnerDto} objects
   * @throws NotFoundException if any of the owners are not found
   */
  @Override
  public Map<Long, OwnerDto> getAllById(Collection<Long> ids) throws NotFoundException {
    LOG.trace("getAllById({})", ids);
    Map<Long, OwnerDto> owners =
        dao.getAllById(ids).stream()
            .map(mapper::entityToDto)
            .collect(Collectors.toUnmodifiableMap(OwnerDto::id, Function.identity()));
    for (final var id : ids) {
      if (!owners.containsKey(id)) {
        throw new NotFoundException("Owner with ID %d not found".formatted(id));
      }
    }
    return owners;
  }

  /**
   * Searches for owners based on search parameters.
   *
   * @param searchParameters the search criteria
   * @return a stream of matching {@link OwnerDto} objects
   */
  @Override
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    return dao.search(searchParameters).stream()
        .map(mapper::entityToDto);
  }

}
