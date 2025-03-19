import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Owner} from '../dto/owner';

const baseUri = environment.backendUrl + '/owners';

@Injectable({
  providedIn: 'root'
})
export class OwnerService {

  constructor(
    private http: HttpClient,
  ) {
  }

  /**
   * Get all owners stored in the system
   *
   * @return observable list of found owners.
   */
  getAll(): Observable<Owner[]> {
    return this.http.get<Owner[]>(baseUri);
  }

  /**
   * Create a new owner in the system.
   *
   * @param owner the data for the owner that should be created
   * @return an Observable for the created owner
   */
  create(owner: Owner): Observable<Owner> {
    return this.http.post<Owner>(
      baseUri,
      owner
    );
  }

  public searchByName(name: string, limitTo: number): Observable<Owner[]> {
    const params = new HttpParams()
      .set('name', name)
      .set('maxAmount', limitTo);
    return this.http.get<Owner[]>(baseUri, {params});
  }

}
