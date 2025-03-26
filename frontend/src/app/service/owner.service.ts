import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Owner} from '../dto/owner';

const baseUri = environment.backendUrl + '/owners';

@Injectable({
  providedIn: 'root'
})

/**
 * Service for managing owner-related API operations in the system.
 */
export class OwnerService {

  constructor(
    private http: HttpClient,
  ) {
  }

  /**
   * Retrieves all owners stored in the system.
   *
   * @returns An Observable containing an array of owner objects
   */
  getAll(): Observable<Owner[]> {
    return this.http.get<Owner[]>(baseUri);
  }

  /**
   * Searches for owners by name with a limit on results.
   *
   * @param name The name to search for
   * @returns An Observable containing an array of matching owner objects (up to 5)
   */
  public searchByName(name: string): Observable<Owner[]> {
    return this.http.get<Owner[]>(
      baseUri, {
        params: {name: name, limit: 5}
      }
    );
  }

  /**
   * Creates a new owner in the system.
   *
   * @param owner The data for the owner to be created
   * @returns An Observable for the created owner
   */
  create(owner: Owner): Observable<Owner> {
    return this.http.post<Owner>(
      baseUri,
      owner
    );
  }
}
