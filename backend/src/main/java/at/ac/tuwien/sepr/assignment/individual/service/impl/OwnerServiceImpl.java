package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailOwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.OwnerMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.OwnerDao;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * Service implementation for managing owner-related operations.
 */
@Service
public class OwnerServiceImpl implements OwnerService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final OwnerDao dao;
  private final OwnerMapper mapper;
  private final OwnerValidator validator;

  public OwnerServiceImpl(OwnerDao dao, OwnerMapper mapper, OwnerValidator validator) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OwnerDto getById(long id) throws NotFoundException {

    LOG.trace("Entering getById [requestId={}]: Retrieving owner with id {}", MDC.get("r"), id);

    OwnerDto owner = mapper.entityToDto(dao.getById(id));

    LOG.debug("Retrieved owner with id {} [requestId={}]: {}", id, MDC.get("r"), owner);

    return owner;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<OwnerDto> getAll() {

    LOG.trace("Entering getAll [requestId={}]: Retrieving all owners", MDC.get("r"));

    Stream<OwnerDto> owners = dao.getAll().stream().map(mapper::entityToDto);

    LOG.debug("Retrieved all owners [requestId={}]: Operation completed", MDC.get("r"));

    return owners;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<Long, HorseDetailOwnerDto> getAllById(Collection<Long> ids) throws NotFoundException {

    LOG.trace("Entering getAllById [requestId={}]: Retrieving owners with ids {}", MDC.get("r"), ids);

    Map<Long, HorseDetailOwnerDto> owners = dao.search(new OwnerSearchDto(null, ids, null)).stream()
        .map(mapper::entityToDto)
        .collect(Collectors.toUnmodifiableMap(OwnerDto::id, OwnerDto::toHorseDetailOwnerDto));

    LOG.debug("Retrieved {} owners for ids {} [requestId={}]", owners.size(), ids, MDC.get("r"));

    for (final var id : ids) {
      if (!owners.containsKey(id)) {
        LOG.warn("Owner with id {} not found in batch retrieval [requestId={}]", id, MDC.get("r"));
        throw new NotFoundException("Owner with ID %d not found".formatted(id));
      }
    }

    return owners;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters) throws ValidationException {

    LOG.trace("Entering search [requestId={}]: Searching owners with parameters {}", MDC.get("r"), searchParameters);

    validator.validateForSearch(searchParameters);

    Stream<OwnerDto> owners = dao.search(searchParameters).stream().map(mapper::entityToDto);

    LOG.debug("Found owners matching search parameters [requestId={}]: Operation completed", MDC.get("r"));

    return owners;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OwnerDto create(OwnerCreateDto owner) throws ValidationException, ConflictException {

    LOG.trace("Entering create [requestId={}]: Creating owner with data {}", MDC.get("r"), owner);

    validator.validateForCreate(owner);

    var createdOwner = dao.create(owner);
    OwnerDto result = mapper.entityToDto(createdOwner);

    LOG.info("Successfully created owner with id {} [requestId={}]", result.id(), MDC.get("r"));

    return result;
  }
}
