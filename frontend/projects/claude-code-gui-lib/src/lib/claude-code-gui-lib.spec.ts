import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClaudeCodeGuiLib } from './claude-code-gui-lib';

describe('ClaudeCodeGuiLib', () => {
  let component: ClaudeCodeGuiLib;
  let fixture: ComponentFixture<ClaudeCodeGuiLib>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClaudeCodeGuiLib]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClaudeCodeGuiLib);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
