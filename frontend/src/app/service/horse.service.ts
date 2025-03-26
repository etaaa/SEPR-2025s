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

/**
 * Service for managing horse-related API operations in the system.
 */
export class HorseService {

  constructor(
    private http: HttpClient,
    private notification: ToastrService
  ) {
  }

  /**
   * Retrieves a horse by its unique identifier.
   *
   * @param id The ID of the horse to retrieve
   * @returns An Observable containing the horse object
   */
  getById(id: number): Observable<Horse> {
    return this.http.get<Horse>(
      `${baseUri}/${id}`
    ).pipe(
      map(this.fixHorseDate)
    );
  }

  /**
   * Retrieves horses from the system, optionally filtered by criteria.
   *
   * @param filters Optional criteria to filter horses, including limit and excludeId
   * @returns An Observable containing an array of horse objects
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

    const hasFilters = Object.values(filters).some(val => val !== undefined && val !== null && val !== '');

    if (hasFilters) {
      if (filters.name) params = params.set('name', filters.name);
      if (filters.description) params = params.set('description', filters.description);
      if (filters.dateOfBirth) params = params.set('dateOfBirth', filters.dateOfBirth);
      if (filters.sex) params = params.set('sex', filters.sex);
      if (filters.owner) params = params.set('ownerName', filters.owner);
      params = params.set('limit', (filters.limit !== undefined ? filters.limit : 100).toString());
      if (filters.excludeId !== undefined) params = params.set('excludeId', filters.excludeId.toString());
    }

    return this.http.get<Horse[]>(baseUri, {params})
      .pipe(
        map(horses => horses.map(this.fixHorseDate))
      );
  }

  /**
   * Creates a new horse in the system.
   *
   * @param formData The form data containing horse details to be created
   * @returns An Observable for the created horse
   */
  create(formData: FormData): Observable<Horse> {
    return this.http.post<Horse>(
      baseUri,
      formData
    ).pipe(
      map(this.fixHorseDate)
    );
  }

  /**
   * Updates an existing horse in the system.
   *
   * @param id The ID of the horse to update
   * @param formData The form data containing updated horse details
   * @returns An Observable for the updated horse
   */
  update(id: number, formData: FormData): Observable<Horse> {
    return this.http.put<Horse>(
      `${baseUri}/${id}`,
      formData
    ).pipe(
      map(this.fixHorseDate)
    );
  }

  /**
   * Deletes a horse from the system by its ID.
   *
   * @param id The ID of the horse to delete
   * @returns An Observable indicating the deletion completion
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(
      `${baseUri}/${id}`
    );
  }

  /**
   * Deletes a horse from the system and provides user feedback via notifications.
   *
   * @param horse The horse object to delete
   * @returns An Observable indicating the deletion completion
   * @throws {Error} If the horse ID is missing
   */
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

  /**
   * Converts the horse's dateOfBirth string to a Date object.
   *
   * @param horse The horse object to process
   * @returns The horse object with dateOfBirth as a Date
   * @private
   */
  private fixHorseDate(horse: Horse): Horse {
    horse.dateOfBirth = new Date(horse.dateOfBirth as unknown as string);
    return horse;
  }
}
