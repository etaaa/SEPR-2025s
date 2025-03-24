import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {tap, catchError} from 'rxjs/operators';
import {environment} from 'src/environments/environment';
import {Horse} from '../dto/horse';
import {ToastrService} from "ngx-toastr";


const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient,
    private notification: ToastrService
  ) {
  }

  getById(id: number): Observable<Horse> {
    return this.http.get<Horse>(
      `${baseUri}/${id}`
    ).pipe(
      map(this.fixHorseDate)
    );
  }

  /**
   * Retrieve horses from the system, optionally filtered by criteria
   *
   * @param filters Optional criteria to filter horses, including limit and excludeId
   * @return observable list of found horses
   */
  getAllOrSearch(filters: {
    name?: string,
    description?: string,
    dateOfBirth?: string,
    sex?: string,
    owner?: string,
    limit?: number,
    excludeId?: number
  } = {}): Observable<Horse[]> {
    let params = new HttpParams();

    let url = baseUri
    const hasFilters = Object.values(filters).some(val => val !== undefined && val !== null && val !== '');

    if (hasFilters) {
      if (filters.name) params = params.set('name', filters.name);
      if (filters.description) params = params.set('description', filters.description);
      if (filters.dateOfBirth) params = params.set('dateOfBirth', filters.dateOfBirth);
      if (filters.sex) params = params.set('sex', filters.sex);
      if (filters.owner) params = params.set('ownerName', filters.owner);
      params = params.set('limit', (filters.limit !== undefined ? filters.limit : 100).toString());
      if (filters.excludeId !== undefined) params = params.set('excludeId', filters.excludeId.toString());
      url += "/search";
    }

    return this.http.get<Horse[]>(url, {params})
      .pipe(
        map(horses => horses.map(this.fixHorseDate))
      );
  }

  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(formData: FormData): Observable<Horse> {
    /*
    console.log(horse);
    // Cast the object to any, so that we can circumvent the type checker.
    // We _need_ the date to be a string here, and just passing the object with the
    // “type error” to the HTTP client is unproblematic
    (horse as any).dateOfBirth = formatIsoDate(horse.dateOfBirth);
    */

    return this.http.post<Horse>(
      baseUri,
      formData
    ).pipe(
      map(this.fixHorseDate)
    );
  }

  update(id: number, formData: FormData): Observable<Horse> {
    return this.http.put<Horse>(
      `${baseUri}/${id}`,
      formData
    ).pipe(
      map(this.fixHorseDate)
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(
      `${baseUri}/${id}`
    );
  }

  deleteHorse(horse: Horse): Observable<void> {
    if (!horse.id) {
      throw new Error('Horse ID is required for deletion');
    }
    return this.delete(horse.id).pipe(
      tap(() => {
        this.notification.success(`Horse ${horse.name} was deleted`, 'Success');
      }),
      catchError(error => {
        console.error('Error deleting horse', error);
        const errorMessage = (error.error && error.error.message) || 'An unknown error occurred';
        console.log('Backend error message:', errorMessage);
        this.notification.error(errorMessage, `Could Not Delete Horse ${horse.name}`);
        throw error;
      })
    );
  }

  private fixHorseDate(horse: Horse): Horse {
    // Parse the string to a Date
    horse.dateOfBirth = new Date(horse.dateOfBirth as unknown as string);
    return horse;
  }
}
