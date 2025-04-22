import { Component, ElementRef, inject, input, OnInit, output, signal, ViewChild } from '@angular/core';
import { ActivityResponse } from '../../model/activity.type';
import { getDate } from '../../shared/utils';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fontawsomeIcons } from '../../shared/fa-icons';
import { AuthService } from '../../service/auth.service';
import { CommentService } from '../../service/comment.service';
import { CommentDto } from '../../model/comment.type';

@Component({
  selector: 'app-comment-box',
  imports: [FontAwesomeModule],
  templateUrl: './comment-box.component.html',
  styleUrl: './comment-box.component.css'
})
export class CommentBoxComponent implements OnInit {
  @ViewChild('message') message!: ElementRef;

  authService = inject(AuthService);
  faLibrary = inject(FaIconLibrary);
  commentService = inject(CommentService);
  
  activity = input.required<ActivityResponse>();
  refresh = output<boolean>();
  
  owner = signal(false);
  util = signal(false);
  edit = signal(false);
  
  ngOnInit(): void {
    this.faLibrary.addIcons(...fontawsomeIcons);
    this.owner.set(this.activity().author === this.authService.getFirstname());
  }

  toggleUtil() {
    this.util.update(toggle => !toggle);
  }

  toggleEdit() {
    this.edit.update(toggle => !toggle);
  }

  delete() {
    this.commentService.deleteComment(this.activity().id).subscribe({
      next: (respones) => {
        this.refresh.emit(true);
      },
      error: (err) => {
        console.error(err);
      }
    })
  }

  updateComment() {
    const message = (this.message.nativeElement as HTMLTextAreaElement).value;
    if (message === this.activity().message) {
      this.edit.set(false);
      return;
    }

    const request: CommentDto = {
      ticketId: 0,
      message: message
    }

    this.commentService.updateCommnet(this.activity().id, request).subscribe({
      next: (response) => {
        this.edit.set(false);
        this.activity().message = response.message;
        this.activity().lastUpdated = response.lastUpdated;
      },
      error: (err) => {
        console.error(err);
      }
    })
  }

  getDate(date: any) {
    return getDate(date);
  }
}
