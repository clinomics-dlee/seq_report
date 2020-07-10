var UserTable = function() {
	
	var drawnTableFunc = {};
	var items = {};
	var pageInfo = {};
	var pageOrder = [];
	var excelDatas = {};
	
	return {
		init : function (uid, pageable, pagesView, pagesVal, isDownloadExcel) {
			//var uid = id.substring(1);
			var h = '<div class="dataTables_wrapper form-inline dt-bootstrap no-footer">';
			h += '<div class="dataTables_length m-b-10" id="' + uid + '_length">';
			
			if (pageable) {
				h += '<label>최대 <select id="' + uid + '_page" aria-controls="' + uid + '_ctrl" class="form-control input-sm">';
				
				if (!pagesView) pagesVal = [ 10, 50 ], pagesView = [ 10, 50 ];
				for (var p in pagesView) {
					h += '<option value="' + pagesVal[p] + '">' + pagesView[p] + '</option>';
				}
				h += '</select> 페이지 표시</label>';
				h += '<input type="hidden" id="' + uid + '_page_start" value="0"/>';
			}
			
			h += '</div>';
			h += '<div class="dt-buttons btn-group pull-right">';
			if (isDownloadExcel) {
				h += '<button id="' + uid + '_sort_btn" class="btn btn-default">정렬 초기화</button>';
				h += '<button id="' + uid + '_excel_btn" class="btn btn-success">엑셀 다운로드</button>';
			}
			h += '</div>';
			h += '<div style="clear:both;"></div>';
			h += '<div class="dataTables_scroll" style="overflow: auto;">';
			h += ' <table id="' + uid + '_table" class="table table-striped table-bordered dataTable" style="width: 100%; margin: 0 0 !important;">';
			h += '  <thead></thead><tbody></tbody>';
			h += ' </table>';
			h += ' <div style="position: absolute; top: 50%; left: 50%; background-color: #555; opacity: 0.7; transform:translate(-50%, -50%); display:none;" class="text-center p-10" id="' + uid + '_loading">';
			h += '  <strong style="color: white;">Loading</strong><br/>';
			h += '  <img src="/assets/img/table-loader.gif">';
			h += ' </div>';
			h += '</div>';
			h += '<div class="row">';
			h += ' <div class="col-sm-5">';
			h += '  <div class="dataTables_info" id="' + uid + '_polite" role="status" aria-live="polite"></div>';
			h += ' </div>';
			h += ' <div class="col-sm-7">';
			h += '  <div id="' + uid + '_paginate" class="m-t-10"></div>';
			h += ' </div>';
			h += '</div>';

			h += '</div>';
			$('#' + uid).html(h);
			
			pageOrder[uid] = "";
			pageInfo[uid] = {};
			pageInfo[uid].pageable = pageable;
			pageInfo[uid].pagesView = pagesView;
			pageInfo[uid].pagesVal = pagesVal;

			$("#" + uid + "_excel_btn").click(function() {
				// step 1. workbook 생성
				var wb = XLSX.utils.book_new();
				// step 2. 시트 만들기 
				var newWorksheet = XLSX.utils.json_to_sheet(excelDatas[uid]);
				// var newWorksheet = XLSX.utils.table_to_sheet(html);
				// step 3. workbook에 새로만든 워크시트에 이름을 주고 붙인다.  
				XLSX.utils.book_append_sheet(wb, newWorksheet, "sheet1");
				// step 4. 엑셀 파일 만들기 
				var wbout = XLSX.write(wb, {bookType:'xlsx',  type: 'binary'});
				// step 5. 엑셀 파일 내보내기 
				saveAs(new Blob([s2ab(wbout)], {type:"application/octet-stream"}), "Sample.xlsx");
			});

			$("#" + uid + "_sort_btn").click(function() {
				pageOrder[uid] = "";
				drawnTableFunc[uid]();
			});
		},
		clear : function (uid) {
			$('#' + uid + "_table tbody").empty();
		},
		reload : function (uid) {
			
			drawnTableFunc[uid]();
		},
		getRowCount : function (uid) {
			return items[uid].length;
		},
		getRows : function (uid) {
			return items[uid];
		},
		getRowAt : function (uid, index) {
			return items[uid][index];
		},
		getCheckedRows : function (uid) {
			var checkedItems = [];
			if (items[uid].length > 0) {
				for (var i = 0; i < items[uid].length; i++) {
					if ($("#" + uid + "_checkbox_" + i).is(":checked")) {
						items[uid][i]["index"] = i;
						checkedItems.push(items[uid][i]);
					}
				}
			}
			
			return checkedItems;
		},
		draw : function (uid, ajax, columns, custom_func) {
			//var uid = id.substring(1);
			var start = $('#' + uid + '_page_start').val();
			var page = $('#' + uid + '_page').val();
			var param;
			if (!ajax.data) {
				param = {};
			} else {
				param = ajax.data();
			}

			param.pgNmb = start;
			param.pgrwc = page;
			param.order = pageOrder[uid];
			
			if (!ajax.type) {
				ajax.type = "GET";
			}
			if (!ajax.dataType) {
				ajax.dataType = "json";
			}
			if (!ajax.contentType) {
				ajax.contentType = "application/json; charset=utf-8";
			}

			$('#' + uid + '_loading').show();
			
			
			$.ajax({
				url : ajax.url,
				type : ajax.type,
				data : param,
				dataType : ajax.dataType,
				contentType : ajax.contentType,
				beforeSend : ajax.beforeSend,
				success: function (rtn) {
					excelDatas[uid] = [];
					items[uid] = rtn.data;
					var rows = rtn.data;
					var html = '';
					var headHtml = '';

					if (custom_func) {
						var htmls = custom_func(rows);
						html = htmls.html;
						headHtml = htmls.headHtml;
					} else {
						
						var aColumns = columns.slice();
						if (rtn.header) {
							for (var r in aColumns) {
								
								if (aColumns[r].serverHeader) {
									var sHeader = rtn.header;
									if (aColumns[r].sorting) {
										for (var sh in sHeader) {
											sHeader[sh].sorting = true;
										}
									}
									sHeader.reverse();
									aColumns.splice(r, 1);
									
									for (var h in sHeader) {
										if (sHeader[h]) aColumns.splice(r, 0, sHeader[h]);
									}
									break;
								}
							}
						}
						
						var rowIndex = 0;
						for (var s in rows) {
							var excelData = {};
							html += '<tr>';
							for (var r in aColumns) {
								var val = '';
								try {
									val = eval('rows[s].' + aColumns[r].data);
								} catch(ex) {
									val = '';
								}
								var width = '', headClassName = ' class="', bodyClassName = '';
								
								if (aColumns[r].width) {
									width = ' style="width:' + aColumns[r].width + ';"';
								}
								if (aColumns[r].headClassName) {
									headClassName = ' ' + aColumns[r].headClassName;
								}
								if (aColumns[r].sorting) {
									var sort = '';
									if (pageOrder[uid].includes(aColumns[r].data + ":asc")) {
										sort = 'sorting_asc';
									} else if (pageOrder[uid].includes(aColumns[r].data + ":desc")) {
										sort = 'sorting_desc';
									} else {
										sort = 'sorting';
									}
									headClassName += sort + ' px-lg-4" sort="' + aColumns[r].data + '"';
								} else {
									headClassName += '"';
								}
								if (aColumns[r].bodyClassName) {
									bodyClassName = ' class="' + aColumns[r].bodyClassName + '"';
								}

								// #. header settings
								if (s == 0 && aColumns[r].title) {
									if (aColumns[r].type != null && aColumns[r].type.constructor == Object) {
										if (aColumns[r].type.name == 'checkbox') {
											headHtml += '<th' + width + headClassName + '>'
												+ aColumns[r].title
												+ '<div class="form-check checkbox checkbox-css p-5">'
												+ '<input type="checkbox" value="" id="' + uid + '_checkbox_all" name="' + uid + '_checkbox_all" data="">'
												+ '<label for="' + uid + '_checkbox_all"></label>'
												+ '</div>'
												+ '</th>';
										} else {
											// #. default header
											headHtml += '<th' + width + headClassName + '>' + aColumns[r].title + '</th>';
										}
									} else {
										// #. default header
										headHtml += '<th' + width + headClassName + '>' + aColumns[r].title + '</th>';
									}

									if (!aColumns[r].hideExcel) {
										excelData[aColumns[r].title] = "";
									}
								}
								
								if (aColumns[r].render) {
									
									var renderValue = aColumns[r].render(rows[Number(s)], val, Number(s), param);
									html += '<td' + bodyClassName + '>' + renderValue + '</td>';
									if (!aColumns[r].hideExcel) {
										// #. rendering 한 값에 tag 제거
										excelData[aColumns[r].title] = renderValue.replace(/(<([^>]+)>)/ig,"");;
									}
								} else {
									if (aColumns[r].type == 'index') {
										
										val = '<strong>' + (Number(s) + 1 + (rtn.pageNumber * page)) + '</strong>';

									} else if (aColumns[r].type == 'rindex') {
										
										val = '<strong>' + (rtn.recordsTotal - (Number(s) + (rtn.pageNumber * page))) + '</strong>';

									} else if (aColumns[r].type == 'date') {
										if (val != null) {
											val = moment(val).format('YYYY-MM-DD');
										} else {
											val = "";
										}
									} else if (aColumns[r].type != null && aColumns[r].type.constructor == Object) {

										var disabled = (aColumns[r].type.disabled ? aColumns[r].type.disabled(rows[Number(s)]) : null);
										
										if (aColumns[r].type.name == 'checkbox') {
											val = '<div class="form-check checkbox checkbox-css p-5">'
												+ '<input type="checkbox" value="" id="' + uid + '_checkbox_'+ rowIndex +'" name="' + uid + '_checkbox" data="'+ val +'">'
												+ '<label for="' + uid + '_checkbox_'+ rowIndex +'"></label>'
												+ '</div>';
										} else if (aColumns[r].type.name == "radio") {
											val = '<div class="radio radio-css">'
												+ '<input type="radio" value="" id="' + uid + '_radio_'+ rowIndex +'" name="' + uid + '_radio" data="'+ val +'">'
												+ '<label for="' + uid + '_radio_'+ rowIndex +'"></label>'
												+ '</div>';
										} else if (aColumns[r].type.name == "combo") {
											var selectValue = val;
											val = '<select class="form-control" style="width:100%;" id="' + uid + '_combo_'+ rowIndex +'" name="' + uid + '_combo">';
											if (aColumns[r].type.isBlank) {
												val += '<option value="">선택</option>';
											}
											for (var key in aColumns[r].type.items) {
												var selected = "";
												if (selectValue == key) {
													selected = "selected";
												}
												val += '<option value="' + key + '" ' + selected + '>' + aColumns[r].type.items[key] + '</option>';
											}
											val += '</select>';
										}
										if (disabled) {
											val = "";
										}
									}
									html += '<td' + bodyClassName + '>' + (val ? val : '') + '</td>';
									if (!aColumns[r].hideExcel) {
										excelData[aColumns[r].title] = (val ? val : '');
									}
								}
								
							}
							html += '</tr>';
							rowIndex++;
							excelDatas[uid].push(excelData);
						}
					}
					headHtml += '</tr>';
					
					$('#' + uid + '_table thead').empty();
					$('#' + uid + '_table tbody').empty();
					if (!html || html == '') {
						html = '<td class="text-center" colspan="' + (headHtml.match(/\/td/g) || []).length + '">Empty</td>';
					}
					if (headHtml == '</tr>') {
						headHtml = '';
					}
					$('#' + uid + '_table thead').html(headHtml);
					$('#' + uid + '_table tbody').html(html);
					
					if (pageInfo[uid].pageable) {
						var n = Number(rtn.pageNumber);
						var t = Number(rtn.recordsTotal);
						var p = Number(page);
						var active = '', disabled = '';
						html = '<div class="dataTables_paginate paging_simple_numbers">';
						html += '<ul class="pagination">';
						if (n == 0) disabled = ' disabled';
						html += '<li class="paginate_button previous' + disabled + '" id="' + uid + '_previous">'
							+ '<a href="#" aria-controls="' + uid + '_ctrl" data-dt-idx="' + (n - 1) + '" tabindex="0">Previous</a>'
							+ '</li>';
						var max = Math.ceil(t / p);
						
						var i = 0;
						if (n == i) active = ' active';
						html += '<li class="paginate_button ' + active + '">'
							+ '<a href="#" aria-controls="' + uid + '_ctrl" data-dt-idx="' + (i) + '" tabindex="0">' + (i + 1) + '</a>'
							+ '</li>';
						
						if (n > 3) {
							html += '<li class="paginate_button disabled"><a href="#" aria-controls="' + uid + '_ctrl" data-dt-idx="6" tabindex="0">…</a></li>';
							if (n > max - 5) {
								i = max - 5;			
							} else {
								i = n - 2;	
							}
						}
						
						for (var j = (i == 0 ? 1 : i); j < max; j++) {
							active = '';
							if (n == j) active = ' active';
							html += '<li class="paginate_button' + active + '">'
								+ '<a href="#" aria-controls="' + uid + '_ctrl" data-dt-idx="' + (j) + '" tabindex="0">' + (j + 1) + '</a>'
								+ '</li>';
							if (j == i + 4 && j < max - 2) {
								active = '';
								if (n == j) active = ' active';
								html += '<li class="paginate_button disabled"><a href="#" aria-controls="' + uid + '_ctrl" data-dt-idx="6" tabindex="0">…</a></li>';
								html += '<li class="paginate_button' + active + '">'
									+ '<a href="#" aria-controls="' + uid + '_ctrl" data-dt-idx="' + (max - 1) + '" tabindex="0">' + max + '</a>'
									+ '</li>';
								break;
							}
							
						}
						disabled = '';
						if (n == max - 1) disabled = ' disabled';
						html += '<li class="paginate_button next' + disabled + '" id="' + uid + '_next">'
							+ '<a href="#" aria-controls="' + uid + '_ctrl" data-dt-idx="' + (n + 1) + '" tabindex="0">Next</a>'
							+ '</li>';
						html += '</ul>';
						html += '</div>';
						
						$('#' + uid + '_paginate').html(html);
						$('#' + uid + '_polite').text('총 ' + t + ' 건의 데이터 / ' + (n * p + 1) + ' ~ ' + (n * p + p) + '');
					}
					
					$('#' + uid + " table thead th").css({'white-space' : 'nowrap', 'word-break' : 'nowrap'});
					$('#' + uid + " table thead th").addClass('text-center');
					$('#' + uid + " table tbody td").css({'white-space' : 'nowrap'
						, 'word-break' : 'nowrap'
						, 'vertical-align' : 'middle !important'});
					
					$('a[aria-controls="' + uid + '_ctrl"]').click(function () {
						var nextval = $(this).attr("data-dt-idx");
						if (!$(this).parent().hasClass("disabled") && !(n == nextval)) {
							$('#' + uid + '_page_start').val(Number(nextval));
							UserTable.draw(uid, ajax, columns, custom_func);
						}
					});
					
					$('a[aria-controls="' + uid + '_ctrl"]').click(function () {
						var nextval = $(this).attr("data-dt-idx");
						if (!$(this).parent().hasClass("disabled") && !(n == nextval)) {
							$('#' + uid + '_page_start').val(Number(nextval));
							UserTable.draw(uid, ajax, columns, custom_func);
						}
					});
					
					drawnTableFunc[uid] = function () {
						UserTable.draw(uid, ajax, columns, custom_func);
					}

					$("#" + uid + "_checkbox_all").change(function() {
						var rowCount = UserTable.getRowCount(uid);
						if (rowCount > 0) {
							var checkedValue = false;
							if ($(this).is(":checked")) {
								checkedValue = true;
							}
							
							for (var i = 0; i < rowCount; i++) {
								$("#" + uid + "_checkbox_" + i).prop("checked", checkedValue);
							}
						}
					});

					$("#" + uid + " th").click(function() {
						var sort = '', removeSort = '';

						if ($(this).hasClass('sorting')) {
							$(this).removeClass('sorting');
							$(this).addClass('sorting_asc');
							sort = 'asc';
							removeSort = 'desc';
						} else if ($(this).hasClass('sorting_asc')) {
							$(this).removeClass('sorting_asc');
							$(this).addClass('sorting_desc');
							sort = 'desc';
							removeSort = 'asc';
						} else if ($(this).hasClass('sorting_desc')) {
							$(this).removeClass('sorting_desc');
							$(this).addClass('sorting_asc');
							sort = 'asc';
							removeSort = 'desc';
						} else {
							return;
						}
						$(this).addClass('sorting_' + sort);
						var headId = $(this).attr('sort');
						if (pageOrder[uid].includes("," + headId + ":" + removeSort)) {
							pageOrder[uid] = pageOrder[uid].replace("," + headId + ":" + removeSort, "");
						}
						pageOrder[uid] = "," + headId + ":" + sort + pageOrder[uid];
						console.log(pageOrder[uid]);
						drawnTableFunc[uid]();
					});
					
					$("#" + uid + "_page").change(function() {
						$("#" + uid + "_page_start").val(0);
						drawnTableFunc[uid]();
					});
					
					$("input[name=" + uid + "_checkbox]").change(function(e) {
						var rowCount = UserTable.getRowCount(uid);
						if (rowCount > 0) {
							var allCheckedValue = true;
							for (var i = 0; i < rowCount; i++) {
								if (!$("#" + uid + "_checkbox_" + i).is(":checked")) {
									allCheckedValue = false;
									break;
								}
							}
							$("#" + uid + "_checkbox_all").prop("checked", allCheckedValue);
						}
					});
					
					$(".default-select2").select2();
					$('#' + uid + '_loading').hide();
				},
				error : function(data) {
					CommonMsg.error(function() { });
					$('#' + uid + '_loading').hide();
				}
			});
		}
	}
}();

toCapitalize = function(text) {
	var text = text.replace(/^./, text[0].toUpperCase());
	return text;
}

camelCaseToTitle = function(text) {
	text = text.replace(/([A-Z])/g, ' $1').replace(/^./, function(str){ return str.toUpperCase(); })
	return text;
}